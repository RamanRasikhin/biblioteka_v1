package com.librarysystem;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class LookupArray implements IPresent {
    private Map<String, Book> presenceMap;
    private IReadWrite dataSource;

    public LookupArray(IReadWrite dataSource) {
        this.dataSource = dataSource;
        this.presenceMap = new HashMap<>();
        refreshPresenceMap();
    }

    public void refreshPresenceMap() {
        this.presenceMap.clear();
        if (this.dataSource != null) {
            for (Book book : dataSource.getAllBooks()) {
                if (book != null && book.getTitle() != null && book.getAuthor() != null) {
                    presenceMap.put(generateKey(book.getTitle(), book.getAuthor()), book);
                }
            }
        }

    }

    private String generateKey(String title, String author) {
        return title.toLowerCase() + "#" + author.toLowerCase();
    }

    @Override
    public void registerBook(Book book) {
        if (book != null && book.getTitle() != null && book.getAuthor() != null) {
            presenceMap.put(generateKey(book.getTitle(), book.getAuthor()), book);
            System.out.println("LookupArray: Noted book '" + book.getTitle() + "'. Map size: " + presenceMap.size());
        }
    }

    @Override
    public boolean isPresent(String title, String author) {
        return presenceMap.containsKey(generateKey(title, author));
    }

    @Override
    public void removeBook(String title, String author) {
        Book removed = presenceMap.remove(generateKey(title, author));
        if (removed != null) {
            System.out.println("LookupArray: De-listed book '" + title + "' by " + author + ". Map size: " + presenceMap.size());
        }
    }

    @Override
    public List<Book> getPresentableBooks() {
        return dataSource.getAllBooks();
    }
}
