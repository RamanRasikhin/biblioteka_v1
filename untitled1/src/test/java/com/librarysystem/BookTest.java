package com.librarysystem;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void testBookCreationAndGetters() {
        Book book = new Book(1, "The Great Gatsby", "F. Scott Fitzgerald", "Classic", "A novel about the American Dream", "1234567890", true);
        assertEquals(1, book.getId());
        assertEquals("The Great Gatsby", book.getTitle());
        assertEquals("F. Scott Fitzgerald", book.getAuthor());
        assertEquals("Classic", book.getGenre());
        assertEquals("A novel about the American Dream", book.getDescription());
        assertEquals("1234567890", book.getIsbn());
        assertTrue(book.isAvailable());
    }

    @Test
    void testBookCreationWithDefaultAvailability() {
        Book book = new Book("To Kill a Mockingbird", "Harper Lee", "Fiction", "A story of justice and racism", "0987654321");
        assertEquals(-1, book.getId());
        assertTrue(book.isAvailable());
        assertEquals("To Kill a Mockingbird", book.getTitle());
    }

    @Test
    void testSetters() {
        Book book = new Book("1984", "George Orwell", "Dystopian", "A totalitarian future", "1122334455");
        book.setAvailable(false);
        assertFalse(book.isAvailable());

        book.setTitle("Nineteen Eighty-Four");
        assertEquals("Nineteen Eighty-Four", book.getTitle());

        book.setId(100);
        assertEquals(100, book.getId());
    }
}