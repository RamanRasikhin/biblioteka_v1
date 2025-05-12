package com.librarysystem;

public interface IPresent {
    void registerBook(com.librarysystem.Book book);
    boolean isPresent(String title, String author);
    void removeBook(String title, String author);
    java.util.List<com.librarysystem.Book> getPresentableBooks();
}
