package com.librarysystem;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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

        Book storedBook = readWrite.findBookById(book.getId());
        if (storedBook == null) {
            throw new IllegalStateException("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") does not exist in the system.");
        }
        if (!storedBook.isAvailable()) {
            throw new IllegalStateException("Book '" + storedBook.getTitle() + "' is not available for borrowing.");
        }
        if (user.getLibraryCard() == null || !user.getLibraryCard().isValid(borrowDate)) {
            throw new IllegalStateException("User's library card is invalid or missing for the borrow date.");
        }

        int currentUserBorrows = countActiveBorrowsForUser(user);
        if (currentUserBorrows >= user.getBookLimit()) {
            throw new IllegalStateException("User has reached the book borrowing limit (" + user.getBookLimit() + "). Currently has " + currentUserBorrows + " active borrows.");
        }

        Borrow borrowAction = new Borrow(storedBook, borrowDate, returnDate, user);

        readWrite.updateBookAvailability(storedBook.getId(), false);
        storedBook.setAvailable(false);
        if (book.getId() == storedBook.getId() && book != storedBook) {
            book.setAvailable(false);
        }

        this.present.registerBook(storedBook);

        user.getLibraryCard().addCommand(borrowAction);
        activeBorrows.add(borrowAction);
        System.out.println("Executor: Book '" + storedBook.getTitle() + "' borrowed by " + user.getName());

        Reservation userReservationForThisBook = activeReservations.stream()
                .filter(res -> res.getBook().getId() == storedBook.getId() &&
                        res.getUser().getId() == user.getId() &&
                        ("PENDING".equals(res.getStatus()) || "READY_FOR_PICKUP".equals(res.getStatus())))
                .findFirst()
                .orElse(null);

        if (userReservationForThisBook != null) {
            userReservationForThisBook.setStatus("FULFILLED");
            user.recieveMessage("Your reservation for '" + storedBook.getTitle() + "' has been fulfilled by borrowing the book.");
            System.out.println("Executor: Reservation for '" + storedBook.getTitle() + "' by " + user.getName() + " marked as FULFILLED.");
        }

        return borrowAction;
    }

    public IAction createReservation(com.librarysystem.Book book, User user, com.librarysystem.Date reservationDate) {
        if (book == null) throw new IllegalArgumentException("Book cannot be null for reservation.");
        if (user == null) throw new IllegalArgumentException("User cannot be null for reservation.");

        Book storedBook = readWrite.findBookById(book.getId());
        if (storedBook == null) {
            throw new IllegalStateException("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") does not exist and cannot be reserved.");
        }

        if (user.getLibraryCard() == null || !user.getLibraryCard().isValid(reservationDate)) {
            throw new IllegalStateException("User's library card is invalid or missing for the reservation date.");
        }

        boolean alreadyBorrowedByUser = activeBorrows.stream()
                .anyMatch(b -> b.getBook().getId() == storedBook.getId() && b.getUser().getId() == user.getId());
        if (alreadyBorrowedByUser) {
            throw new IllegalStateException("User '" + user.getName() + "' has already borrowed this book ('" + storedBook.getTitle() + "'). Cannot reserve.");
        }

        boolean existingActiveReservation = activeReservations.stream()
                .anyMatch(r -> r.getBook().getId() == storedBook.getId() &&
                        r.getUser().getId() == user.getId() &&
                        ("PENDING".equals(r.getStatus()) || "READY_FOR_PICKUP".equals(r.getStatus())));
        if (existingActiveReservation) {
            throw new IllegalStateException("User '" + user.getName() + "' already has an active reservation for this book ('" + storedBook.getTitle() + "').");
        }

        Reservation reservationAction = new Reservation(storedBook, reservationDate, "PENDING", user);
        user.getLibraryCard().addCommand(reservationAction);
        activeReservations.add(reservationAction);
        System.out.println("Executor: Book '" + storedBook.getTitle() + "' reserved by " + user.getName());
        user.recieveMessage("Book '" + storedBook.getTitle() + "' has been reserved. Reservation date: " + reservationDate);
        return reservationAction;
    }

    public void addBook(com.librarysystem.Book book) {
        readWrite.registerBook(book);
        present.registerBook(book);
    }

    public void returnBook(com.librarysystem.Book book, User user) {
        if (book == null) throw new IllegalArgumentException("Book to return cannot be null.");
        if (user == null) throw new IllegalArgumentException("User returning book cannot be null.");

        Book storedBook = readWrite.findBookById(book.getId());
        if (storedBook == null) {
            throw new IllegalStateException("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") not found in system for return.");
        }

        Borrow toRemove = null;
        for (Borrow borrow : activeBorrows) {
            if (borrow.getBook().getId() == storedBook.getId() && borrow.getUser().getId() == user.getId()) {
                toRemove = borrow;
                break;
            }
        }

        if (toRemove != null) {
            activeBorrows.remove(toRemove);

            readWrite.updateBookAvailability(storedBook.getId(), true);
            storedBook.setAvailable(true);
            if (book.getId() == storedBook.getId() && book != storedBook) {
                book.setAvailable(true);
            }

            present.registerBook(storedBook);

            System.out.println("Executor: Book '" + storedBook.getTitle() + "' returned by " + user.getName());

            Reservation nextReservation = activeReservations.stream()
                    .filter(res -> res.getBook().getId() == storedBook.getId() && "PENDING".equals(res.getStatus()))
                    .findFirst().orElse(null);

            if (nextReservation != null) {
                nextReservation.setStatus("READY_FOR_PICKUP");
                if (nextReservation.getUser() != null) {
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
        List<Book> allBooks = readWrite.getAllBooks();
        if (allBooks == null) allBooks = new ArrayList<>();

        for (Book b : allBooks) {
            if (b.getTitle().equals(title) && b.getAuthor().equals(author)) {
                foundBookInStorage = b;
                break;
            }
        }

        if (foundBookInStorage != null) {
            int bookId = foundBookInStorage.getId();
            boolean canRemove = true;
            if (activeBorrows.stream().anyMatch(b -> b.getBook().getId() == bookId)) {
                System.out.println("Executor: Cannot remove book '" + title + "', it is currently borrowed.");
                canRemove = false;
            }
            if (canRemove && activeReservations.stream().anyMatch(r -> r.getBook().getId() == bookId &&
                    ("PENDING".equals(r.getStatus()) || "READY_FOR_PICKUP".equals(r.getStatus())))) {
                System.out.println("Executor: Cannot remove book '" + title + "', it has active reservations.");
                canRemove = false;
            }

            if (canRemove) {
                readWrite.removeBook(bookId);
                present.removeBook(title, author);
                activeReservations.removeIf(r -> r.getBook().getId() == bookId);
                System.out.println("Executor: Book '" + title + "' removed.");
            }
        } else {
            System.out.println("Executor: Book not found for removal: " + title);
        }
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(activeReservations);
    }

    public List<Reservation> getActiveUserReservations(User user) {
        if (user == null) return new ArrayList<>();
        return activeReservations.stream()
                .filter(r -> r.getUser().getId() == user.getId() &&
                        ("PENDING".equals(r.getStatus()) || "READY_FOR_PICKUP".equals(r.getStatus())))
                .collect(Collectors.toList());
    }

    public List<Reservation> getAllActiveReservations() {
        return activeReservations.stream()
                .filter(r -> ("PENDING".equals(r.getStatus()) || "READY_FOR_PICKUP".equals(r.getStatus())))
                .collect(Collectors.toList());
    }

    public List<Borrow> getBorrows() {
        return new ArrayList<>(activeBorrows);
    }

    public List<Book> listBooks() {
        return readWrite.getAllBooks();
    }

    public void executeBorrow(Borrow borrow) {
        System.out.println("Executing borrow for: " + borrow.getBook().getTitle() + " by " + borrow.getUser().getName());
    }

    public void executeReservation(Reservation reservation) {
        System.out.println("Executing reservation for: " + reservation.getBook().getTitle() + " by " + reservation.getUser().getName());
    }

    public void checkAndNotifyForUpcomingReturns(Date currentDate, int daysInAdvance) {
        if (currentDate == null) {
            currentDate = Date.getCurrentDate();
        }
        Date notificationThresholdDate = currentDate.plusDays(daysInAdvance);

        for (Borrow borrow : activeBorrows) {
            if (borrow.isReminderSentForThisPeriod()) {
                continue;
            }

            User user = borrow.getUser();
            Book book = borrow.getBook();
            Date returnDate = borrow.getReturnDate();

            if (returnDate.isSameDayOrAfter(currentDate) && returnDate.isBefore(notificationThresholdDate.plusDays(1))) {
                String message = "Reminder: The book '" + book.getTitle() +
                        "' is due for return on " + returnDate.toString() + ".";
                user.recieveMessage(message);
                borrow.setReminderSentForThisPeriod(true);
                System.out.println("Executor: Sent return reminder for '" + book.getTitle() + "' (due: " + returnDate + ") to " + user.getName());
            }
        }
    }
}
