package com.librarysystem;

import java.util.ArrayList;
import java.util.List;

// Corresponds to UML Gateway
public class Gateway {
    private com.librarysystem.IPresent present; // Could be LookupArray
    private com.librarysystem.IReadWrite readWrite; // Could be Storage
    private com.librarysystem.Executor executor; // Handles core logic

    // Constructor from UML
    public Gateway(com.librarysystem.IPresent present, com.librarysystem.IReadWrite readWrite) {
        this.present = present;
        this.readWrite = readWrite;
        // Executor needs IPresent and IReadWrite, so we can pass them through
        this.executor = new com.librarysystem.Executor(present, readWrite);
    }

    // Methods from UML, often delegating to Executor or directly to Present/ReadWrite
    public void createReservation(com.librarysystem.Book book, com.librarysystem.User user, com.librarysystem.Date reservationDate) {
        executor.createReservation(book, user, reservationDate);
    }

    public void createBorrow(com.librarysystem.Book book, com.librarysystem.User user, com.librarysystem.Date borrowDate, com.librarysystem.Date returnDate) {
        executor.createBorrow(book, user, borrowDate, returnDate);
    }

    public void returnBook(com.librarysystem.Book book, com.librarysystem.User user) {
        executor.returnBook(book, user);
    }

    public void addBook(com.librarysystem.Book book) { // From C++ Pracownik logic
        executor.addBook(book);
    }

    public void removeBookById(int bookId) { // From C++ Pracownik logic, using ID
        com.librarysystem.Book book = readWrite.findBookById(bookId);
        if (book != null) {
            // Ensure book is not currently borrowed or reserved before actual removal
            // For simplicity, direct removal via executor
            executor.removeBook(book.getTitle(), book.getAuthor());
        } else {
            System.out.println("Gateway: Book with ID " + bookId + " not found for removal.");
        }
    }

    public List<com.librarysystem.Book> listAvailableBooks() {
        // This would typically filter books from executor.listBooks() or present.getPresentableBooks()
        List<com.librarysystem.Book> allBooks = present.getPresentableBooks();
        List<com.librarysystem.Book> availableBooks = new ArrayList<>();
        for (com.librarysystem.Book book : allBooks) {
            if (book.isAvailable()) {
                availableBooks.add(book);
            }
        }
        return availableBooks;
    }

    public List<com.librarysystem.Book> searchBooks(String searchTerm) { // Simplified search
        List<com.librarysystem.Book> allBooks = present.getPresentableBooks();
        List<com.librarysystem.Book> foundBooks = new ArrayList<>();
        String lowerSearchTerm = searchTerm.toLowerCase();
        for (com.librarysystem.Book book : allBooks) {
            if (book.getTitle().toLowerCase().contains(lowerSearchTerm) ||
                    book.getAuthor().toLowerCase().contains(lowerSearchTerm) ||
                    book.getIsbn().toLowerCase().contains(lowerSearchTerm) ||
                    book.getGenre().toLowerCase().contains(lowerSearchTerm)) {
                foundBooks.add(book);
            }
        }
        return foundBooks;
    }

    public com.librarysystem.Book findBookById(int id) {
        return readWrite.findBookById(id);
    }

    // User management facade methods (delegating to AccessManager, which isn't directly in Gateway's UML fields)
    // For this example, we'll assume AccessManager is accessible or these are handled differently.
    // If strict to UML, Gateway wouldn't directly call AccessManager unless it had a reference.
    // Let's assume Main class coordinates AccessManager and Gateway.

    // Methods to interact with Executor for borrows/reservations lists
    public List<com.librarysystem.Borrow> getUserBorrows(com.librarysystem.User user) {
        List<com.librarysystem.Borrow> userBorrows = new ArrayList<>();
        for (com.librarysystem.Borrow b : executor.getBorrows()) {
            if (b.getUser().getId() == user.getId()) {
                userBorrows.add(b);
            }
        }
        return userBorrows;
    }

    public List<com.librarysystem.Reservation> getUserReservations(com.librarysystem.User user) {
        List<com.librarysystem.Reservation> userReservations = new ArrayList<>();
        for (com.librarysystem.Reservation r : executor.getReservations()) {
            if (r.getUser().getId() == user.getId()) {
                userReservations.add(r);
            }
        }
        return userReservations;
    }

    public List<com.librarysystem.Reservation> getAllReservations() {
        return executor.getReservations();
    }

    public List<com.librarysystem.Borrow> getAllBorrows(com.librarysystem.Date currentDate) { // Add currentDate for overdue status
        List<com.librarysystem.Borrow> borrows = executor.getBorrows();
        // Optionally enrich with overdue status here if needed by UI directly
        return borrows;
    }
}
