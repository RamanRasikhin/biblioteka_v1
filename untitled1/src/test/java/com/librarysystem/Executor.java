// ============== File: com/librarysystem/Executor.java (MODIFIED) ==============
package com.librarysystem;

import java.util.List;
import java.util.ArrayList;

public class Executor {
    private IPresent present;
    private IReadWrite readWrite;
    private List<Borrow> activeBorrows;
    private List<Reservation> activeReservations;

    public Executor(IPresent present, IReadWrite readWrite) {
        this.present = present;
        this.readWrite = readWrite;
        this.activeBorrows = new ArrayList<>();
        this.activeReservations = new ArrayList<>();
    }

    // ***** ADDED HELPER METHOD *****
    public int countActiveBorrowsForUser(User user) {
        if (user == null) return 0;
        int count = 0;
        for (Borrow borrow : this.activeBorrows) {
            if (borrow.getUser().getId() == user.getId()) {
                count++;
            }
        }
        return count;
    }

    public IAction createBorrow(com.librarysystem.Book book, User user, com.librarysystem.Date borrowDate, com.librarysystem.Date returnDate) {
        if (book == null) throw new IllegalArgumentException("Book cannot be null for borrowing.");
        if (user == null) throw new IllegalArgumentException("User cannot be null for borrowing.");

        // Sprawdzenie, czy książka istnieje w systemie
        Book storedBook = readWrite.findBookById(book.getId());
        if (storedBook == null) {
            throw new IllegalStateException("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") does not exist in the system.");
        }
        if (!storedBook.isAvailable()) { // Sprawdzaj stan ze Storage (storedBook to obiekt ze Storage)
            throw new IllegalStateException("Book '" + storedBook.getTitle() + "' is not available for borrowing.");
        }
        if (user.getLibraryCard() == null || !user.getLibraryCard().isValid(Date.getCurrentDate())) {
            throw new IllegalStateException("User's library card is invalid or missing.");
        }

        // ***** MODIFIED BOOK LIMIT CHECK *****
        int currentUserBorrows = countActiveBorrowsForUser(user);
        if (currentUserBorrows >= user.getBookLimit()) {
            throw new IllegalStateException("User has reached the book borrowing limit (" + user.getBookLimit() + "). Currently has " + currentUserBorrows + " active borrows.");
        }

        Borrow borrowAction = new Borrow(storedBook, borrowDate, returnDate, user); // Use storedBook

        // Aktualizuj stan książki w Storage
        readWrite.updateBookAvailability(storedBook.getId(), false);
        // Książka w pamięci (parametr book / storedBook) też powinna być zaktualizowana
        storedBook.setAvailable(false);
        // If the 'book' parameter might be a different instance than 'storedBook', update it too if necessary,
        // but using 'storedBook' consistently after fetching is safer.
        if (book.getId() == storedBook.getId() && book != storedBook) {
            book.setAvailable(false);
        }


        // Inform IPresent (LookupArray) about the change in the book's state
        // This ensures that if LookupArray's presenceMap holds this book,
        // it's updated with the new availability status.
        this.present.registerBook(storedBook); // Use storedBook which has been updated

        user.getLibraryCard().addCommand(borrowAction);
        activeBorrows.add(borrowAction);
        System.out.println("Executor: Book '" + storedBook.getTitle() + "' borrowed by " + user.getName());
        return borrowAction;
    }

    public IAction createReservation(com.librarysystem.Book book, User user, com.librarysystem.Date reservationDate) {
        if (book == null) throw new IllegalArgumentException("Book cannot be null for reservation.");
        if (user == null) throw new IllegalArgumentException("User cannot be null for reservation.");

        // Sprawdzenie, czy książka istnieje w systemie
        Book storedBook = readWrite.findBookById(book.getId());
        if (storedBook == null) {
            throw new IllegalStateException("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") does not exist and cannot be reserved.");
        }

        if (user.getLibraryCard() == null || !user.getLibraryCard().isValid(Date.getCurrentDate())) {
            throw new IllegalStateException("User's library card is invalid or missing.");
        }

        Reservation reservationAction = new Reservation(storedBook, reservationDate, "PENDING", user); // Use storedBook
        user.getLibraryCard().addCommand(reservationAction);
        activeReservations.add(reservationAction);
        System.out.println("Executor: Book '" + storedBook.getTitle() + "' reserved by " + user.getName());
        user.recieveMessage("Book '" + storedBook.getTitle() + "' has been reserved. Reservation date: " + reservationDate);
        return reservationAction;
    }


    public void addBook(com.librarysystem.Book book) {
        readWrite.registerBook(book); // To nada ID i zapisze do Storage
        present.registerBook(book);   // To tylko odnotuje w LookupArray (już nie będzie drugiego zapisu do Storage)
    }

    public void returnBook(com.librarysystem.Book book, User user) {
        if (book == null) throw new IllegalArgumentException("Book to return cannot be null.");
        if (user == null) throw new IllegalArgumentException("User returning book cannot be null.");

        // Ensure we are working with the book instance from storage for consistency
        Book storedBook = readWrite.findBookById(book.getId());
        if (storedBook == null) {
            throw new IllegalStateException("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") not found in system for return.");
        }


        Borrow toRemove = null;
        for (Borrow borrow : activeBorrows) {
            // Compare by ID for user and book
            if (borrow.getBook().getId() == storedBook.getId() && borrow.getUser().getId() == user.getId()) {
                toRemove = borrow;
                break;
            }
        }

        if (toRemove != null) {
            activeBorrows.remove(toRemove);

            // Aktualizuj stan książki w Storage
            readWrite.updateBookAvailability(storedBook.getId(), true);
            // Książka w pamięci (parametr book) też powinna być zaktualizowana
            storedBook.setAvailable(true);
            if (book.getId() == storedBook.getId() && book != storedBook) {
                book.setAvailable(true);
            }


            // Poinformuj IPresent o zmianie
            present.registerBook(storedBook); // "Odświeża" wpis w LookupArray z aktualnym stanem storedBook

            System.out.println("Executor: Book '" + storedBook.getTitle() + "' returned by " + user.getName());

            Reservation nextReservation = activeReservations.stream()
                    .filter(res -> res.getBook().getId() == storedBook.getId() && "PENDING".equals(res.getStatus()))
                    .findFirst().orElse(null);

            if (nextReservation != null) {
                nextReservation.setStatus("READY_FOR_PICKUP");
                if (nextReservation.getUser() != null) { // Dodatkowe sprawdzenie
                    nextReservation.getUser().recieveMessage(
                            "Book '" + storedBook.getTitle() + "' you reserved is now available for pickup!"
                    );
                }
            }
        } else {
            throw new IllegalStateException("Book '" + storedBook.getTitle() + "' was not recorded as borrowed by " + user.getName());
        }
    }

    public void removeBook(String title, String author) {
        Book foundBookInStorage = null;
        // Najpierw znajdź książkę w Storage, aby uzyskać poprawne ID
        List<Book> allBooks = readWrite.getAllBooks(); // Fetch once
        if (allBooks == null) allBooks = new ArrayList<>(); // Safety for tests or empty storage

        for (Book b : allBooks) {
            if (b.getTitle().equals(title) && b.getAuthor().equals(author)) {
                foundBookInStorage = b;
                break;
            }
        }

        if (foundBookInStorage != null) {
            // Sprawdź warunki przed usunięciem
            int bookId = foundBookInStorage.getId();
            boolean canRemove = true;
            if (activeBorrows.stream().anyMatch(b -> b.getBook().getId() == bookId)) {
                System.out.println("Executor: Cannot remove book '" + title + "', it is currently borrowed.");
                canRemove = false;
            }
            // Only check PENDING or READY_FOR_PICKUP reservations, not historical ones
            if (canRemove && activeReservations.stream().anyMatch(r -> r.getBook().getId() == bookId &&
                    ("PENDING".equals(r.getStatus()) || "READY_FOR_PICKUP".equals(r.getStatus())))) {
                System.out.println("Executor: Cannot remove book '" + title + "', it has active reservations.");
                canRemove = false;
            }

            if (canRemove) {
                readWrite.removeBook(bookId);          // Usuń ze Storage
                present.removeBook(title, author);     // Usuń z LookupArray
                // Also remove any completed/cancelled reservations for this book
                activeReservations.removeIf(r -> r.getBook().getId() == bookId);
                System.out.println("Executor: Book '" + title + "' removed.");
            }
        } else {
            System.out.println("Executor: Book not found for removal: " + title);
        }
    }

    // ... (getBorrows, getReservations, listBooks)
    public List<Reservation> getReservations() {
        return new ArrayList<>(activeReservations);
    }

    public List<Borrow> getBorrows() {
        return new ArrayList<>(activeBorrows);
    }

    public List<Book> listBooks() {
        return readWrite.getAllBooks();
    }
    public void executeBorrow(Borrow borrow) {
        System.out.println("Executing borrow for: " + borrow.getBook().getTitle() + " by " + borrow.getUser().getName());
        // Additional logic for when a borrow action is processed by the visitor pattern.
        // Could be logging, audit trails, or post-borrow actions not handled in createBorrow.
        // For now, createBorrow handles the primary state changes.
    }

    public void executeReservation(Reservation reservation) {
        System.out.println("Executing reservation for: " + reservation.getBook().getTitle() + " by " + reservation.getUser().getName());
        // Similar to executeBorrow, this is for visitor pattern processing.
        // Could notify related systems, log, etc.
        // The core reservation logic (adding to list, initial notification) is in createReservation.
        // Logic for fulfilling reservation is in returnBook.
    }
}