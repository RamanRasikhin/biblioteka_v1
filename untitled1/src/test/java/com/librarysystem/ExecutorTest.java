package com.librarysystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class ExecutorTest {

    private static final String TEST_BOOKS_FILE = "executor_test_books.csv";
    private Executor executor;
    private Storage testStorage;
    private LookupArray testLookupArray;
    private User testUser;
    private User otherUser;
    private Book testBook1;
    private Book testBook2;

    private Date simulatedTestCurrentDate;

    @BeforeEach
    void setUp() throws IOException {
        File testFile = new File(TEST_BOOKS_FILE);
        if (testFile.exists()) {
            if (!testFile.delete()) {
                System.err.println("Warning: Could not delete test books file: " + TEST_BOOKS_FILE);
            }
        }
        System.out.println("ExecutorTest.setUp: Deleted old test books file if existed.");

        testStorage = new Storage(TEST_BOOKS_FILE);
        testLookupArray = new LookupArray(testStorage);
        executor = new Executor(testLookupArray, testStorage);
        System.out.println("ExecutorTest.setUp: Initialized new Storage, LookupArray, Executor for " + TEST_BOOKS_FILE);

        simulatedTestCurrentDate = new Date(2025, 5, 9);

        Date cardExpiryForTestUser = simulatedTestCurrentDate.addMonths(6);
        Date cardExpiryForOtherUser = simulatedTestCurrentDate.addMonths(12);

        testUser = new User(1, "Test", "User", "executor.user@example.com", "READER", "pass", 5, cardExpiryForTestUser);
        otherUser = new User(2, "Other", "Borrower", "other@example.com", "READER", "pass", 2, cardExpiryForOtherUser);

        assertNotNull(testUser.getLibraryCard(), "TestUser should have a library card.");
        assertTrue(testUser.getLibraryCard().isValid(simulatedTestCurrentDate),
                "TestUser's card should be valid on " + simulatedTestCurrentDate + ". Expiry: " + testUser.getLibraryCard().getExpiryDate());

        assertNotNull(otherUser.getLibraryCard(), "OtherUser should have a library card.");
        assertTrue(otherUser.getLibraryCard().isValid(simulatedTestCurrentDate),
                "OtherUser's card should be valid on " + simulatedTestCurrentDate + ". Expiry: " + otherUser.getLibraryCard().getExpiryDate());


        testBook1 = new Book("Executor Test Book 1", "Author E1", "Genre E1", "Desc E1", "ISBN_E1");
        testBook2 = new Book("Executor Test Book 2", "Author E2", "Genre E2", "Desc E2", "ISBN_E2");

        testStorage.registerBook(testBook1);
        testStorage.registerBook(testBook2);
        System.out.println("ExecutorTest.setUp: Registered test books. Book1 ID: " + testBook1.getId() + " ("+testBook1.getTitle()+"), Book2 ID: " + testBook2.getId() + " ("+testBook2.getTitle()+")");
    }

    @AfterEach
    void tearDown() throws IOException {
        File testFile = new File(TEST_BOOKS_FILE);
        if (testFile.exists()) {
            if(!testFile.delete()){
                System.err.println("Warning: Could not delete test books file in tearDown: " + TEST_BOOKS_FILE);
            }
        }
        testStorage = null;
        testLookupArray = null;
        executor = null;
        testUser = null;
        otherUser = null;
        testBook1 = null;
        testBook2 = null;
        simulatedTestCurrentDate = null;
        System.out.println("ExecutorTest.tearDown: Cleaned up test file and references.");
        System.out.println("-----------------------------------------------------");
    }


    @Test
    void testCreateBorrow_success() {
        System.out.println("Running testCreateBorrow_success...");
        Book bookFromStorage = testStorage.findBookById(testBook1.getId());
        assertNotNull(bookFromStorage, "Book1 must exist in storage for this test.");
        assertTrue(bookFromStorage.isAvailable(), "Book1 should be available in storage initially.");

        assertTrue(testUser.getLibraryCard().isValid(simulatedTestCurrentDate), "TestUser's card must be valid for this test.");

        Date borrowDate = simulatedTestCurrentDate;
        Date returnDate = borrowDate.addMonths(1);

        IAction borrowAction = executor.createBorrow(bookFromStorage, testUser, borrowDate, returnDate);

        assertNotNull(borrowAction, "Borrow action should not be null.");
        assertTrue(borrowAction instanceof Borrow, "Action should be an instance of Borrow.");

        Book bookAfterBorrow = testStorage.findBookById(testBook1.getId());
        assertNotNull(bookAfterBorrow, "Book1 should still exist in storage.");
        assertFalse(bookAfterBorrow.isAvailable(), "Book1 should be unavailable in storage after borrowing.");

        assertEquals(1, executor.getBorrows().size(), "There should be one borrow recorded in executor.");
        assertEquals(testBook1.getId(), executor.getBorrows().get(0).getBook().getId(), "The borrowed book ID should match.");
        System.out.println("Finished testCreateBorrow_success.");
    }

    @Test
    void testCreateBorrow_bookNotAvailable() {
        System.out.println("Running testCreateBorrow_bookNotAvailable...");
        testStorage.updateBookAvailability(testBook1.getId(), false);

        Book bookFromStorage = testStorage.findBookById(testBook1.getId());
        assertNotNull(bookFromStorage, "Book1 must exist in storage for this test.");
        assertFalse(bookFromStorage.isAvailable(), "Book1 must be set to unavailable for this test.");

        Date borrowDate = simulatedTestCurrentDate;
        Date returnDate = borrowDate.addMonths(1);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            executor.createBorrow(bookFromStorage, testUser, borrowDate, returnDate);
        });
        assertEquals("Book '" + bookFromStorage.getTitle() + "' is not available for borrowing.", exception.getMessage());
        System.out.println("Finished testCreateBorrow_bookNotAvailable.");
    }
    @Test
    void testCreateBorrow_bookDoesNotExist() {
        System.out.println("Running testCreateBorrow_bookDoesNotExist...");
        Book nonExistentBook = new Book(999, "Ghost Book", "Non Existent", "Fiction", "None", "GHOSTISBN", true);

        Date borrowDate = simulatedTestCurrentDate;
        Date returnDate = borrowDate.addMonths(1);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            executor.createBorrow(nonExistentBook, testUser, borrowDate, returnDate);
        });
        assertEquals("Book '" + nonExistentBook.getTitle() + "' (ID: " + nonExistentBook.getId() + ") does not exist in the system.", exception.getMessage());
        System.out.println("Finished testCreateBorrow_bookDoesNotExist.");
    }

    @Test
    void testCreateBorrow_limitReached() {
        System.out.println("Running testCreateBorrow_limitReached...");
        testUser.setBookLimit(1);
        Book book1FromStorage = testStorage.findBookById(testBook1.getId());
        Book book2FromStorage = testStorage.findBookById(testBook2.getId());
        assertNotNull(book1FromStorage);
        assertNotNull(book2FromStorage);

        assertTrue(book1FromStorage.isAvailable(), "Book1 should be available.");
        assertTrue(book2FromStorage.isAvailable(), "Book2 should be available.");

        Date borrowDate = simulatedTestCurrentDate;
        Date returnDate = borrowDate.addMonths(1);

        assertDoesNotThrow(() -> executor.createBorrow(book1FromStorage, testUser, borrowDate, returnDate));
        assertEquals(1, executor.countActiveBorrowsForUser(testUser), "User should have 1 borrow.");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            executor.createBorrow(book2FromStorage, testUser, borrowDate, returnDate);
        });
        assertEquals("User has reached the book borrowing limit (1). Currently has 1 active borrows.", exception.getMessage());
        System.out.println("Finished testCreateBorrow_limitReached.");
    }

    @Test
    void testCreateBorrow_invalidCard_blocked() {
        System.out.println("Running testCreateBorrow_invalidCard_blocked...");
        testUser.getLibraryCard().setBlocked(true);
        assertTrue(testUser.getLibraryCard().isBlocked(), "TestUser's card must be blocked for this test.");
        Book bookFromStorage = testStorage.findBookById(testBook1.getId());
        assertNotNull(bookFromStorage);

        Date borrowDate = simulatedTestCurrentDate;
        Date returnDate = borrowDate.addMonths(1);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            executor.createBorrow(bookFromStorage, testUser, borrowDate, returnDate);
        });
        assertEquals("User's library card is invalid or missing for the borrow date.", exception.getMessage());
        testUser.getLibraryCard().setBlocked(false);
        System.out.println("Finished testCreateBorrow_invalidCard_blocked.");
    }

    @Test
    void testCreateBorrow_invalidCard_expired() {
        System.out.println("Running testCreateBorrow_invalidCard_expired...");
        Date expiredDate = simulatedTestCurrentDate.addMonths(-1);
        testUser.getLibraryCard().setExpiryDate(expiredDate);
        assertFalse(testUser.getLibraryCard().isValid(simulatedTestCurrentDate), "Card should be expired for this test.");
        Book bookFromStorage = testStorage.findBookById(testBook1.getId());
        assertNotNull(bookFromStorage);

        Date borrowDate = simulatedTestCurrentDate;
        Date returnDate = borrowDate.addMonths(1);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            executor.createBorrow(bookFromStorage, testUser, borrowDate, returnDate);
        });
        assertEquals("User's library card is invalid or missing for the borrow date.", exception.getMessage());
        System.out.println("Finished testCreateBorrow_invalidCard_expired.");
    }


    @Test
    void testCreateReservationAndReturnBook_triggersNotification() {
        System.out.println("Running testCreateReservationAndReturnBook_triggersNotification...");
        Book bookToBorrowAndReserve = testStorage.findBookById(testBook1.getId());
        assertNotNull(bookToBorrowAndReserve);
        assertTrue(bookToBorrowAndReserve.isAvailable(), "Book1 should be initially available in storage.");
        assertTrue(otherUser.getLibraryCard().isValid(simulatedTestCurrentDate), "OtherUser's card must be valid.");

        executor.createBorrow(bookToBorrowAndReserve, otherUser, simulatedTestCurrentDate, simulatedTestCurrentDate.addMonths(1));

        Book bookAfterBorrow = testStorage.findBookById(bookToBorrowAndReserve.getId());
        assertNotNull(bookAfterBorrow);
        assertFalse(bookAfterBorrow.isAvailable(), "Book1 should be unavailable after otherUser borrows it.");

        assertTrue(testUser.getLibraryCard().isValid(simulatedTestCurrentDate), "TestUser's card must be valid for reservation.");
        IAction reservationAction = executor.createReservation(bookToBorrowAndReserve, testUser, simulatedTestCurrentDate);
        assertNotNull(reservationAction, "Reservation action should not be null.");
        assertTrue(reservationAction instanceof Reservation, "Action should be Reservation.");

        assertEquals(1, executor.getActiveUserReservations(testUser).size(), "TestUser should have one active reservation.");

        Reservation userReservation = (Reservation) reservationAction;
        assertEquals("PENDING", userReservation.getStatus(), "Reservation status should be PENDING.");
        assertTrue(testUser.getNotifications().stream().anyMatch(n -> n.contains("has been reserved")), "TestUser should get reservation confirmation.");

        if(testUser instanceof User && ((User)testUser).getNotifications() != null) {
            ((User)testUser).clearNotifications();
        }

        executor.returnBook(bookToBorrowAndReserve, otherUser);

        Book bookAfterReturn = testStorage.findBookById(bookToBorrowAndReserve.getId());
        assertNotNull(bookAfterReturn);
        assertTrue(bookAfterReturn.isAvailable(), "Book1 should be available in storage after otherUser returns it.");

        Reservation updatedReservation = executor.getAllReservations().stream()
                .filter(r -> r.getBook().getId() == bookToBorrowAndReserve.getId() &&
                        r.getUser().getId() == testUser.getId() &&
                        "READY_FOR_PICKUP".equals(r.getStatus()))
                .findFirst().orElse(null);
        assertNotNull(updatedReservation, "TestUser's reservation should exist and be READY_FOR_PICKUP.");
        
        boolean notificationFound = testUser.getNotifications().stream()
                .anyMatch(n -> n.contains("you reserved is now available for pickup"));
        assertTrue(notificationFound, "TestUser should be notified that the reserved book is available for pickup.");
        System.out.println("Finished testCreateReservationAndReturnBook_triggersNotification.");
    }

    @Test
    void testReturnBook_notBorrowed() {
        System.out.println("Running testReturnBook_notBorrowed...");
        Book bookNotBorrowed = testStorage.findBookById(testBook2.getId());
        assertNotNull(bookNotBorrowed);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            executor.returnBook(bookNotBorrowed, testUser);
        });
        assertEquals("Book '" + bookNotBorrowed.getTitle() + "' was not recorded as borrowed by " + testUser.getName(), exception.getMessage());
        System.out.println("Finished testReturnBook_notBorrowed.");
    }
}
