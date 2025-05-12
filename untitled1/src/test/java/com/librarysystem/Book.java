package com.librarysystem;

public class Book {
    private String title;
    private String author;
    private String genre;
    private String description;
    private String isbn;

    private boolean available;
    private int id;

    public Book(int id, String title, String author, String genre, String description, String isbn, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.description = description;
        this.isbn = isbn;
        this.available = available;
    }

    public Book(String title, String author, String genre, String description, String isbn) {
        this(-1, title, author, genre, description, isbn, true);
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public String getDescription() { return description; }
    public String getIsbn() { return isbn; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setDescription(String description) { this.description = description; }
    public void setIsbn(String isbn) { this.isbn = isbn; }


    public int getId() { return id; }
    public void setId(int id) { this.id = id;}

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "Book [id=" + id + ", title=" + title + ", author=" + author + ", genre=" + genre
                + ", isbn=" + isbn + ", available=" + available + "]";
    }
}
