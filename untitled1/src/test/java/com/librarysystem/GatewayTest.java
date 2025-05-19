package com.librarysystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GatewayTest {

    private static final String TEST_GW_BOOKS_FILE = "gateway_test_books.csv";
    private static final String TEST_GW_USERS_FILE = "gateway_test_users.csv";

    private Gateway gateway;
    private Storage testStorage;
    private LookupArray testLookupArray;
    private AccessManager testAccessManager;

    private User readerUser;
    private Book book1;
    private Date simulatedTestCurrentDate;

    @BeforeEach
    void setUp() throws IOException {
        File booksFile = new File(TEST_GW_BOOKS_FILE);
        if (booksFile.exists()) booksFile.delete();
        File usersFile = new File(TEST_GW_USERS_FILE);
        if (usersFile.exists()) usersFile.delete();
        System.out.println("GatewayTest.setUp: Deleted old test files.");

        testStorage = new Storage(TEST_GW_BOOKS_FILE);
        testLookupArray = new LookupArray(testStorage);
        testAccessManager = new AccessManager(TEST_GW_USERS_FILE);
        gateway = new Gateway(testLookupArray, testStorage);
        System.out.println("GatewayTest.setUp: Initialized components.");

        simulatedTestCurrentDate = new Date(2025, 5, 10);
        Date cardExpiry = simulatedTestCurrentDate.addMonths(12);

        testAccessManager.createUser("Gateway Reader", "Test", "gw.reader@example.com",
                "READER", "gwpass", 3, cardExpiry);
        readerUser = testAccessManager.login("gw.reader@example.com", "gwpass");
        assertNotNull(readerUser, "ReaderUser setup failed.");
        assertTrue(readerUser.getLibraryCard().isValid(simulatedTestCurrentDate), "ReaderUser's card should be valid at setup.");

        book1 = new Book("Gateway Test Book", "Author GW", "Genre GW", "Desc GW", "ISBN_GW1");
        gateway.addBook(book1);
        assertNotEquals(-1, book1.getId(), "book1 should have an ID after adding via gateway.");
        System.out.println("GatewayTest.setUp: Created user and book1 (ID: " + book1.getId() + ").");
    }

    @AfterEach
    void tearDown() throws IOException {
        new File(TEST_GW_BOOKS_FILE).delete();
        new File(TEST_GW_USERS_FILE).delete();
        System.out.println("GatewayTest.tearDown: Cleaned up test files.");
        System.out.println("-----------------------------------------------------");
    }

    @Test
    void testAddBookThroughGateway() {
        System.out.println("Running testAddBookThroughGateway...");
        Book foundBookInStorage = testStorage.findBookById(book1.getId());
        assertNotNull(foundBookInStorage, "Book1 should be found in storage after adding via gateway.");
        assertEquals("Gateway Test Book", foundBookInStorage.getTitle());

        testLookupArray.refreshPresenceMap();
        assertTrue(testLookupArray.isPresent("Gateway Test Book", "Author GW"),
                "Book1 should be present in LookupArray.");
        System.out.println("Finished testAddBookThroughGateway.");
    }

    @Test
    void testBorrowAndReturnBookThroughGateway() {
        System.out.println("Running testBorrowAndReturnBookThroughGateway...");
        Book targetBook = testStorage.findBookById(book1.getId());
        assertNotNull(targetBook, "Target book (book1) should be found in storage.");
        assertTrue(targetBook.isAvailable(), "Target book should initially be available.");
        assertTrue(readerUser.getLibraryCard().isValid(simulatedTestCurrentDate), "Reader's card must be valid.");

        Date borrowDate = simulatedTestCurrentDate;
        Date returnDate = borrowDate.addMonths(1);

        gateway.createBorrow(targetBook, readerUser, borrowDate, returnDate);

        Book borrowedBookState = testStorage.findBookById(targetBook.getId());
        assertNotNull(borrowedBookState, "Book should still exist in storage after borrow.");
        assertFalse(borrowedBookState.isAvailable(), "Book in storage should be unavailable after gateway borrow.");

        Book bookInLookup = testLookupArray.getPresentableBooks().stream()
                .filter(b -> b.getId() == targetBook.getId())
                .findFirst().orElse(null);
        assertNotNull(bookInLookup, "Book should be findable via LookupArray's getPresentableBooks.");
        assertFalse(bookInLookup.isAvailable(), "Book presented by LookupArray should reflect unavailability.");


        gateway.returnBook(targetBook, readerUser);

        Book returnedBookState = testStorage.findBookById(targetBook.getId());
        assertNotNull(returnedBookState, "Book should exist in storage after return.");
        assertTrue(returnedBookState.isAvailable(), "Book in storage should be available after gateway return.");

        bookInLookup = testLookupArray.getPresentableBooks().stream()
                .filter(b -> b.getId() == targetBook.getId())
                .findFirst().orElse(null);
        assertNotNull(bookInLookup, "Book should be findable via LookupArray's getPresentableBooks after return.");
        assertTrue(bookInLookup.isAvailable(), "Book presented by LookupArray should reflect availability after return.");
        System.out.println("Finished testBorrowAndReturnBookThroughGateway.");
    }

    @Test
    void testListAvailableBooks() {
        System.out.println("Running testListAvailableBooks...");

        Book book2 = new Book("Unavailable Book", "Author UB", "Genre UB", "Desc UB", "ISBN_UB");
        gateway.addBook(book2);
        assertNotEquals(-1, book2.getId());
        Book book2FromStorage = testStorage.findBookById(book2.getId());
        assertNotNull(book2FromStorage);

        assertTrue(readerUser.getLibraryCard().isValid(simulatedTestCurrentDate), "Card must be valid to borrow book2");
        gateway.createBorrow(book2FromStorage, readerUser, simulatedTestCurrentDate, simulatedTestCurrentDate.addMonths(1));

        Book book2AfterBorrow = testStorage.findBookById(book2.getId());
        assertNotNull(book2AfterBorrow, "Book2 should exist in storage.");
        assertFalse(book2AfterBorrow.isAvailable(), "Book2 should be unavailable after borrowing.");

        List<Book> availableBooks = gateway.listAvailableBooks();
        assertEquals(1, availableBooks.size(), "Should only list one available book (book1).");
        assertEquals(book1.getId(), availableBooks.get(0).getId(), "The available book should be book1.");
        System.out.println("Finished testListAvailableBooks.");
    }

    @Test
    void testSearchBooks() {
        System.out.println("Running testSearchBooks...");
        Book bookX = new Book("Specific Search Title", "Author X", "Genre XSpecific", "Desc X", "ISBN_X");
        gateway.addBook(bookX);
        assertNotEquals(-1, bookX.getId());

        testLookupArray.refreshPresenceMap();

        List<Book> resultsTitle = gateway.searchBooks("Specific Search");
        assertEquals(1, resultsTitle.size(), "Search for 'Specific Search' should find 1 book.");
        assertEquals(bookX.getId(), resultsTitle.get(0).getId());

        List<Book> resultsAuthor = gateway.searchBooks("Author GW");
        assertEquals(1, resultsAuthor.size(), "Search for 'Author GW' should find 1 book.");
        assertEquals(book1.getId(), resultsAuthor.get(0).getId());

        List<Book> resultsGenre = gateway.searchBooks("XSpecific");
        assertEquals(1, resultsGenre.size(), "Search for 'XSpecific' should find 1 book.");
        assertEquals(bookX.getId(), resultsGenre.get(0).getId());

        List<Book> resultsNotFound = gateway.searchBooks("NonExistentCriteria");
        assertTrue(resultsNotFound.isEmpty(), "Search for non-existent criteria should return empty list.");
        System.out.println("Finished testSearchBooks.");
    }
}