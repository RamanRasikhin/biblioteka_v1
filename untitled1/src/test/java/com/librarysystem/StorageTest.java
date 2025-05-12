package com.librarysystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageTest {

    private static final String TEST_BOOKS_FILE_PATH = "test_books.csv";
    private Storage storage;

    @BeforeEach
    void setUp() throws IOException {
        File testFile = new File(TEST_BOOKS_FILE_PATH);
        if (testFile.exists()) {
            if (!testFile.delete()) {
                System.err.println("Warning: Could not delete test books file: " + TEST_BOOKS_FILE_PATH);
            }
        }
        System.out.println("StorageTest.setUp: Deleted old test file if existed: " + TEST_BOOKS_FILE_PATH);

        storage = new Storage(TEST_BOOKS_FILE_PATH);
        System.out.println("StorageTest.setUp: New Storage instance created for: " + TEST_BOOKS_FILE_PATH);
    }

    @AfterEach
    void tearDown() throws IOException {
        File testFile = new File(TEST_BOOKS_FILE_PATH);
        if (testFile.exists()) {
            if (!testFile.delete()) {
                System.err.println("Warning: Could not delete test books file in tearDown: " + TEST_BOOKS_FILE_PATH);
            }
        }
        storage = null;
        System.out.println("StorageTest.tearDown: Cleaned up test file and references for: " + TEST_BOOKS_FILE_PATH);
        System.out.println("-----------------------------------------------------");
    }

    @Test
    void testRegisterAndGetBook() {
        System.out.println("Running testRegisterAndGetBook...");
        Book book1 = new Book("The Hobbit", "J.R.R. Tolkien", "Fantasy", "An adventure", "100");
        storage.registerBook(book1);

        assertNotEquals(-1, book1.getId(), "Book ID should be assigned upon registration");
        Book retrievedBook = storage.findBookById(book1.getId());
        assertNotNull(retrievedBook, "Retrieved book should not be null");
        assertEquals("The Hobbit", retrievedBook.getTitle());
        assertTrue(retrievedBook.isAvailable());
        System.out.println("Finished testRegisterAndGetBook.");
    }

    @Test
    void testGetAllBooks() {
        System.out.println("Running testGetAllBooks...");
        assertTrue(storage.getAllBooks().isEmpty(), "Initially, book list should be empty.");

        storage.registerBook(new Book("Book A", "Author A", "Genre A", "Desc A", "ISBN_A"));
        storage.registerBook(new Book("Book B", "Author B", "Genre B", "Desc B", "ISBN_B"));

        List<Book> books = storage.getAllBooks();
        assertEquals(2, books.size(), "Should have 2 books after registration.");
        System.out.println("Finished testGetAllBooks.");
    }

    @Test
    void testRemoveBook() {
        System.out.println("Running testRemoveBook...");
        Book book = new Book("To Remove", "Author R", "Genre R", "Desc R", "ISBN_R");
        storage.registerBook(book);
        int bookId = book.getId();

        assertNotNull(storage.findBookById(bookId), "Book should exist before removal.");
        assertEquals(1, storage.getAllBooks().size(), "Should be 1 book before removal.");

        storage.removeBook(bookId);

        assertNull(storage.findBookById(bookId), "Book should be null after removal.");
        assertTrue(storage.getAllBooks().isEmpty(), "Book list should be empty after removing the only book.");
        System.out.println("Finished testRemoveBook.");
    }

    @Test
    void testUpdateBookAvailability() throws IOException {
        System.out.println("Running testUpdateBookAvailability...");
        Book book = new Book("Availability Test", "Author AT", "Genre AT", "Desc AT", "ISBN_AT");
        storage.registerBook(book);
        int bookId = book.getId();

        Book currentBook = storage.findBookById(bookId);
        assertNotNull(currentBook);
        assertTrue(currentBook.isAvailable(), "Book should be initially available.");

        storage.updateBookAvailability(bookId, false);
        currentBook = storage.findBookById(bookId);
        assertNotNull(currentBook);
        assertFalse(currentBook.isAvailable(), "Book should be unavailable after update.");

        Storage newStorage = new Storage(TEST_BOOKS_FILE_PATH);
        Book reloadedBook = newStorage.findBookById(bookId);
        assertNotNull(reloadedBook, "Book should be found in new Storage instance.");
        assertFalse(reloadedBook.isAvailable(), "Availability change should persist across instances.");

        storage.updateBookAvailability(bookId, true);
        currentBook = storage.findBookById(bookId);
        assertNotNull(currentBook);
        assertTrue(currentBook.isAvailable(), "Book should be available again after second update.");
        System.out.println("Finished testUpdateBookAvailability.");
    }

    @Test
    void testPersistenceAcrossInstances() throws IOException {
        System.out.println("Running testPersistenceAcrossInstances...");
        Book book1 = new Book("Persist Me", "Author P", "Genre P", "Desc P", "ISBN_P1");
        storage.registerBook(book1);
        int id1 = book1.getId();

        Book book2 = new Book(-1, "Another One", "Author O", "Genre O", "Desc O", "ISBN_P2", false);
        storage.registerBook(book2);
        int id2 = book2.getId();

        assertEquals(2, storage.getAllBooks().size(), "Current storage should have 2 books before creating fresh one.");

        Storage freshStorage = new Storage(TEST_BOOKS_FILE_PATH);
        assertEquals(2, freshStorage.getAllBooks().size(), "Fresh storage should load 2 books from file.");

        Book reloadedBook1 = freshStorage.findBookById(id1);
        assertNotNull(reloadedBook1, "Reloaded Book1 should not be null.");
        assertEquals("Persist Me", reloadedBook1.getTitle());
        assertTrue(reloadedBook1.isAvailable());

        Book reloadedBook2 = freshStorage.findBookById(id2);
        assertNotNull(reloadedBook2, "Reloaded Book2 should not be null.");
        assertEquals("Another One", reloadedBook2.getTitle());
        assertFalse(reloadedBook2.isAvailable());
        System.out.println("Finished testPersistenceAcrossInstances.");
    }
}