package com.librarysystem;

import java.util.List;

public interface IReadWrite {
    void removeBook(int bookId);
    void registerBook(com.librarysystem.Book book);
    List<com.librarysystem.Book> getAllBooks();
    com.librarysystem.Book findBookById(int bookId);
    void updateBookAvailability(int bookId, boolean available);
}
