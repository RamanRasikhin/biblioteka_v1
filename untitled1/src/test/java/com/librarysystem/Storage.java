package com.librarysystem;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.*; // For file operations

// Corresponds to UML Storage, implements IReadWrite
public class Storage implements com.librarysystem.IReadWrite {
    private Map<Integer, com.librarysystem.Book> bookListMap; // UML: book_list_map: map (ID as key)
    private String booksFilePath = "books.csv"; // For persistence


    public Storage() { this("books.csv"); }
    public Storage(String booksFilePath) {
        this.bookListMap = new HashMap<>();
        this.booksFilePath = booksFilePath; // Użyj przekazanej ścieżki
        loadBooks(); // Załaduj książki z tej ścieżki
    }

    @Override
    public void removeBook(int bookId) { // Changed from (title, author) to ID to match UML better
        if (bookListMap.containsKey(bookId)) {
            bookListMap.remove(bookId);
            saveBooks();
            System.out.println("Book ID " + bookId + " removed from storage.");
        } else {
            System.out.println("Book ID " + bookId + " not found in storage for removal.");
        }
    }

    @Override
    public void registerBook(com.librarysystem.Book book) {
        if (book.getId() == -1) { // New book without pre-assigned ID
            int newId = bookListMap.keySet().stream().mapToInt(k -> k).max().orElse(0) + 1;
            book.setId(newId);
        }
        bookListMap.put(book.getId(), book);
        saveBooks();
        System.out.println("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") registered in storage.");
    }

    @Override
    public List<com.librarysystem.Book> getAllBooks() {
        return new ArrayList<>(bookListMap.values()); // Return a copy
    }

    @Override
    public com.librarysystem.Book findBookById(int bookId) {
        return bookListMap.get(bookId);
    }

    @Override
    public void updateBookAvailability(int bookId, boolean available) {
        com.librarysystem.Book book = bookListMap.get(bookId);
        if (book != null) {
            book.setAvailable(available);
            saveBooks(); // Persist the change
        }
    }

    // Persistence (simplified CSV)
    private void loadBooks() {
        File file = new File(booksFilePath);
        if (!file.exists()) {
            System.out.println("Books file not found, starting fresh.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";", -1); // -1 to keep empty trailing strings
                if (parts.length >= 7) {
                    int id = Integer.parseInt(parts[0]);
                    String title = parts[1];
                    String author = parts[2];
                    String genre = parts[3];
                    String description = parts[4];
                    String isbn = parts[5];
                    boolean available = Boolean.parseBoolean(parts[6]);
                    com.librarysystem.Book book = new com.librarysystem.Book(id, title, author, genre, description, isbn, available);
                    bookListMap.put(book.getId(), book);
                }
            }
            System.out.println("Loaded " + bookListMap.size() + " books.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading books: " + e.getMessage());
        }
    }

    private void saveBooks() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(booksFilePath))) {
            bw.write("ID;Title;Author;Genre;Description;ISBN;Available\n");
            for (com.librarysystem.Book book : bookListMap.values()) {
                bw.write(book.getId() + ";");
                bw.write(book.getTitle() + ";");
                bw.write(book.getAuthor() + ";");
                bw.write(book.getGenre() + ";");
                bw.write(book.getDescription() + ";");
                bw.write(book.getIsbn() + ";");
                bw.write(book.isAvailable() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving books: " + e.getMessage());
        }
    }
}
