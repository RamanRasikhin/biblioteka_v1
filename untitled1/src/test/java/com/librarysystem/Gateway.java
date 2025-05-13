package com.librarysystem;

import java.util.ArrayList;
import java.util.List;

public class Gateway {
    private com.librarysystem.IPresent present;
    private com.librarysystem.IReadWrite readWrite;
    private com.librarysystem.Executor executor;

    public Gateway(com.librarysystem.IPresent present, com.librarysystem.IReadWrite readWrite) {
        this.present = present;
        this.readWrite = readWrite;
        this.executor = new com.librarysystem.Executor(present, readWrite);
    }

    public Executor getExecutor() {
        return this.executor;
    }

    public void createReservation(com.librarysystem.Book book, com.librarysystem.User user, com.librarysystem.Date reservationDate) {
        executor.createReservation(book, user, reservationDate);
    }

    public void createBorrow(com.librarysystem.Book book, com.librarysystem.User user, com.librarysystem.Date borrowDate, com.librarysystem.Date returnDate) {
        executor.createBorrow(book, user, borrowDate, returnDate);
    }

    public void returnBook(com.librarysystem.Book book, com.librarysystem.User user) {
        executor.returnBook(book, user);
    }

    public void addBook(com.librarysystem.Book book) {
        executor.addBook(book);
    }

    public void removeBookById(int bookId) {
        com.librarysystem.Book book = readWrite.findBookById(bookId);
        if (book != null) {
            executor.removeBook(book.getTitle(), book.getAuthor());
        } else {
            System.out.println("Gateway: Book with ID " + bookId + " not found for removal.");
        }
    }

    public List<com.librarysystem.Book> listAvailableBooks() {
        List<com.librarysystem.Book> allBooks = present.getPresentableBooks();
        List<com.librarysystem.Book> availableBooks = new ArrayList<>();
        for (com.librarysystem.Book book : allBooks) {
            if (book.isAvailable()) {
                availableBooks.add(book);
            }
        }
        return availableBooks;
    }

    public List<com.librarysystem.Book> searchBooks(String searchTerm) {
        List<com.librarysystem.Book> allBooks = present.getPresentableBooks();
        List<com.librarysystem.Book> foundBooks = new ArrayList<>();
        String lowerSearchTerm = searchTerm.toLowerCase();
        for (com.librarysystem.Book book : allBooks) {
            if (book.getTitle().toLowerCase().contains(lowerSearchTerm) ||
                    book.getAuthor().toLowerCase().contains(lowerSearchTerm) ||
                    (book.getIsbn() != null && book.getIsbn().toLowerCase().contains(lowerSearchTerm)) ||
                    (book.getGenre() != null && book.getGenre().toLowerCase().contains(lowerSearchTerm))) {
                foundBooks.add(book);
            }
        }
        return foundBooks;
    }

    public com.librarysystem.Book findBookById(int id) {
        return readWrite.findBookById(id);
    }

    public List<com.librarysystem.Borrow> getUserBorrows(com.librarysystem.User user) {
        List<com.librarysystem.Borrow> userBorrows = new ArrayList<>();
        if (user == null) return userBorrows;
        for (com.librarysystem.Borrow b : executor.getBorrows()) {
            if (b.getUser().getId() == user.getId()) {
                userBorrows.add(b);
            }
        }
        return userBorrows;
    }

    public List<com.librarysystem.Reservation> getUserActiveReservations(com.librarysystem.User user) {
        return executor.getActiveUserReservations(user);
    }

    public List<com.librarysystem.Reservation> getAllActiveReservations() {
        return executor.getAllActiveReservations();
    }

    public List<com.librarysystem.Reservation> getAllReservationsHistory() {
        return executor.getAllReservations();
    }

    public List<com.librarysystem.Borrow> getAllBorrows(com.librarysystem.Date currentDate) {
        return executor.getBorrows();
    }
}
