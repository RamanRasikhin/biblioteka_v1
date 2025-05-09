package com.librarysystem;

// Corresponds to UML IPresent
public interface IPresent {
    void registerBook(com.librarysystem.Book book); // UML: register_book(book: Book): void
    boolean isPresent(String title, String author); // UML: is_present(title: string, author: string): bool
    void removeBook(String title, String author); // UML: remove_book(title: string, author: string): void
    // May need a way to get all presentable books for UI
    java.util.List<com.librarysystem.Book> getPresentableBooks();
}
