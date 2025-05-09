package com.librarysystem;

import java.util.List;

// Corresponds to UML IReadWrite
public interface IReadWrite {
    void removeBook(int bookId); // UML: remove_book(title: string, author: string): void - adapting to ID from Storage
    void registerBook(com.librarysystem.Book book); // UML: register_book(book: Book): void
    List<com.librarysystem.Book> getAllBooks(); // UML: get_all_books(): list<string>{frozen} - returning Book objects is more useful
    com.librarysystem.Book findBookById(int bookId);
    void updateBookAvailability(int bookId, boolean available); // For persistence
}
