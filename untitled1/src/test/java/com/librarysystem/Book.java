package com.librarysystem;

// Corresponds to UML Book
public class Book {
    // attributes from UML
    private String title;
    private String author;
    private String genre;
    private String description;
    private String isbn; // Assuming id from Ksiazka.h can be mapped to ISBN or a unique ID

    // Additional field to manage availability, as in C++ Ksiazka
    private boolean available;
    private int id; // From C++ Ksiazka

    public Book(int id, String title, String author, String genre, String description, String isbn, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.description = description;
        this.isbn = isbn; // Or generate one if not directly provided
        this.available = available;
    }

    // Constructor from UML
    public Book(String title, String author, String genre, String description, String isbn) {
        this(-1, title, author, genre, description, isbn, true); // Default ID and availability
    }


    // Getters from UML
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public String getDescription() { return description; }
    public String getIsbn() { return isbn; } // Or getId if ISBN is used as ID

    // Setters (optional, based on immutability preference, UML implies they exist implicitly)
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setDescription(String description) { this.description = description; }
    public void setIsbn(String isbn) { this.isbn = isbn; }


    // Methods from C++ Ksiazka, adapted
    public int getId() { return id; } // To match C++ logic
    public void setId(int id) { this.id = id;}

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "Book [id=" + id + ", title=" + title + ", author=" + author + ", genre=" + genre
                + ", isbn=" + isbn + ", available=" + available + "]";
    }
}
