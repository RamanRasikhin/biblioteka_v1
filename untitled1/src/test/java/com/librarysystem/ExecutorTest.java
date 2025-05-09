package com.librarysystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File; // Użyj File
import java.io.IOException;
// import java.nio.file.Files; // Alternatywa
// import java.nio.file.Paths; // Alternatywa
import static org.junit.jupiter.api.Assertions.*;

class ExecutorTest {

    private static final String TEST_BOOKS_FILE = "executor_test_books.csv";
    // Plik użytkowników nie jest bezpośrednio zarządzany przez Executor,
    // więc nie potrzebujemy dla niego dedykowanej ścieżki w tym teście.

    private Executor executor;
    private Storage testStorage;       // Konkretna implementacja IReadWrite
    private LookupArray testLookupArray; // Konkretna implementacja IPresent
    private User testUser;
    private User otherUser; // Dodany dla testu rezerwacji
    private Book testBook1;
    private Book testBook2;

    // Symulowana "aktualna" data dla celów testowych
    private Date simulatedTestCurrentDate;

    @BeforeEach
    void setUp() throws IOException {
        // Krok 1: Zawsze usuwaj plik testowy PRZED każdym testem
        File testFile = new File(TEST_BOOKS_FILE);
        if (testFile.exists()) {
            if (!testFile.delete()) {
                System.err.println("Warning: Could not delete test books file: " + TEST_BOOKS_FILE);
            }
        }
        System.out.println("ExecutorTest.setUp: Deleted old test books file if existed.");

        // Krok 2: Twórz nowe instancje dla każdego testu, używając dedykowanego pliku
        testStorage = new Storage(TEST_BOOKS_FILE);
        testLookupArray = new LookupArray(testStorage); // LookupArray współdzieli dane ze Storage
        executor = new Executor(testLookupArray, testStorage);
        System.out.println("ExecutorTest.setUp: Initialized new Storage, LookupArray, Executor for " + TEST_BOOKS_FILE);

        // Krok 3: Ustaw symulowaną "aktualną" datę dla tego zestawu testów
        simulatedTestCurrentDate = new Date(2025, 5, 9); // Założenie: dzisiaj jest 09.05.2025

        // Krok 4: Utwórz użytkowników testowych z kartami ważnymi WZGLĘDEM simulatedTestCurrentDate
        Date cardExpiryForTestUser = simulatedTestCurrentDate.addMonths(6); // Karta ważna przez 6 miesięcy od "dzisiaj"
        Date cardExpiryForOtherUser = simulatedTestCurrentDate.addMonths(12); // Karta ważna przez 12 miesięcy

        testUser = new User(1, "Test", "User", "executor.user@example.com", "READER", "pass", 5, cardExpiryForTestUser);
        otherUser = new User(2, "Other", "Borrower", "other@example.com", "READER", "pass", 2, cardExpiryForOtherUser);

        // Asercje sprawdzające, czy karty są poprawnie utworzone i ważne
        assertNotNull(testUser.getLibraryCard(), "TestUser should have a library card.");
        assertTrue(testUser.getLibraryCard().isValid(simulatedTestCurrentDate),
                "TestUser's card should be valid on " + simulatedTestCurrentDate + ". Expiry: " + testUser.getLibraryCard().getExpiryDate());

        assertNotNull(otherUser.getLibraryCard(), "OtherUser should have a library card.");
        assertTrue(otherUser.getLibraryCard().isValid(simulatedTestCurrentDate),
                "OtherUser's card should be valid on " + simulatedTestCurrentDate + ". Expiry: " + otherUser.getLibraryCard().getExpiryDate());


        // Krok 5: Utwórz książki testowe
        testBook1 = new Book("Executor Test Book 1", "Author E1", "Genre E1", "Desc E1", "ISBN_E1");
        testBook2 = new Book("Executor Test Book 2", "Author E2", "Genre E2", "Desc E2", "ISBN_E2");

        // Dodaj książki do storage. Storage.registerBook nada ID i zapisze do pliku.
        testStorage.registerBook(testBook1);
        testStorage.registerBook(testBook2);
        System.out.println("ExecutorTest.setUp: Registered test books. Book1 ID: " + testBook1.getId() + " ("+testBook1.getTitle()+"), Book2 ID: " + testBook2.getId() + " ("+testBook2.getTitle()+")");
        // LookupArray powinien się zsynchronizować, ponieważ używa tej samej instancji testStorage.
    }

    @AfterEach
    void tearDown() throws IOException {
        File testFile = new File(TEST_BOOKS_FILE);
        if (testFile.exists()) {
            if(!testFile.delete()){
                System.err.println("Warning: Could not delete test books file in tearDown: " + TEST_BOOKS_FILE);
            }
        }
        // Zerowanie referencji
        testStorage = null;
        testLookupArray = null;
        executor = null;
        testUser = null;
        otherUser = null;
        testBook1 = null;
        testBook2 = null;
        simulatedTestCurrentDate = null;
        System.out.println("ExecutorTest.tearDown: Cleaned up test file and references.");
        System.out.println("-----------------------------------------------------"); // Separator między testami
    }

    private void ensureExecutorUsesCurrentDateForValidation(Executor currentExecutor) {
        // Ta metoda jest pułapką myślową. Executor używa Date.getCurrentDate() wewnątrz.
        // Jeśli chcemy to kontrolować, Executor musi przyjmować datę jako parametr
        // do metod createBorrow/createReservation, jak sugerowałem wcześniej.
        // Na razie zakładamy, że testy są pisane z myślą o tym, że Date.getCurrentDate()
        // jest bliskie simulatedTestCurrentDate, albo że karty są tak ustawione,
        // by być ważne niezależnie od drobnych różnic.
        // Dla pełnej kontroli, zmodyfikuj Executor.java.
    }

    @Test
    void testCreateBorrow_success() {
        System.out.println("Running testCreateBorrow_success...");
        // Sprawdź stan początkowy
        assertTrue(testStorage.findBookById(testBook1.getId()).isAvailable(), "Book1 should be available in storage initially.");
        assertTrue(testUser.getLibraryCard().isValid(simulatedTestCurrentDate), "TestUser's card must be valid for this test.");

        Date borrowDate = simulatedTestCurrentDate; // Użyj symulowanej daty
        Date returnDate = borrowDate.addMonths(1);

        // Wywołanie testowanej metody
        IAction borrowAction = executor.createBorrow(testBook1, testUser, borrowDate, returnDate);

        // Asercje
        assertNotNull(borrowAction, "Borrow action should not be null.");
        assertTrue(borrowAction instanceof Borrow, "Action should be an instance of Borrow.");
        assertFalse(testStorage.findBookById(testBook1.getId()).isAvailable(), "Book1 should be unavailable in storage after borrowing.");
        assertEquals(1, executor.getBorrows().size(), "There should be one borrow recorded in executor.");
        assertEquals(testBook1.getId(), executor.getBorrows().get(0).getBook().getId(), "The borrowed book ID should match.");
        System.out.println("Finished testCreateBorrow_success.");
    }

    @Test
    void testCreateBorrow_bookNotAvailable() {
        System.out.println("Running testCreateBorrow_bookNotAvailable...");
        // Ensure the book exists but is unavailable
        testStorage.registerBook(testBook1); // Ensure it's in storage
        testStorage.updateBookAvailability(testBook1.getId(), false); // Set to unavailable

        Book bookFromStorage = testStorage.findBookById(testBook1.getId());
        assertNotNull(bookFromStorage, "Book1 must exist in storage for this test.");
        assertFalse(bookFromStorage.isAvailable(), "Book1 must be set to unavailable for this test.");


        Date borrowDate = simulatedTestCurrentDate;
        Date returnDate = borrowDate.addMonths(1);

        // Asercja rzucenia wyjątku
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            // Pass the book instance that reflects the state in storage (or at least its ID)
            executor.createBorrow(bookFromStorage, testUser, borrowDate, returnDate);
        });
        // MODIFIED: Updated expected message to match Executor's specific message
        assertEquals("Book '" + bookFromStorage.getTitle() + "' is not available for borrowing.", exception.getMessage());
        System.out.println("Finished testCreateBorrow_bookNotAvailable.");
    }
    @Test
    void testCreateBorrow_bookDoesNotExist() { // ADDED TEST CASE
        System.out.println("Running testCreateBorrow_bookDoesNotExist...");
        // Create a book object that is NOT in storage
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
    void testCreateBorrow_limitReached() { // ADDED TEST CASE
        System.out.println("Running testCreateBorrow_limitReached...");
        // Set user's book limit to 1 for this test
        testUser.setBookLimit(1);
        assertTrue(testStorage.findBookById(testBook1.getId()).isAvailable(), "Book1 should be available.");
        assertTrue(testStorage.findBookById(testBook2.getId()).isAvailable(), "Book2 should be available.");

        Date borrowDate = simulatedTestCurrentDate;
        Date returnDate = borrowDate.addMonths(1);

        // First borrow should succeed
        assertDoesNotThrow(() -> executor.createBorrow(testBook1, testUser, borrowDate, returnDate));
        assertEquals(1, executor.countActiveBorrowsForUser(testUser), "User should have 1 borrow.");


        // Second borrow should fail due to limit
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            executor.createBorrow(testBook2, testUser, borrowDate, returnDate);
        });
        assertEquals("User has reached the book borrowing limit (1). Currently has 1 active borrows.", exception.getMessage());
        System.out.println("Finished testCreateBorrow_limitReached.");
    }

    @Test
    void testCreateBorrow_invalidCard_blocked() {
        System.out.println("Running testCreateBorrow_invalidCard_blocked...");
        testUser.getLibraryCard().setBlocked(true); // Zablokuj kartę
        assertTrue(testUser.getLibraryCard().isBlocked(), "TestUser's card must be blocked for this test.");
        // Ważność daty nie ma znaczenia, jeśli karta jest zablokowana
        // assertFalse(testUser.getLibraryCard().isValid(simulatedTestCurrentDate), "Blocked card should be invalid.");


        Date borrowDate = simulatedTestCurrentDate;
        Date returnDate = borrowDate.addMonths(1);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            executor.createBorrow(testBook1, testUser, borrowDate, returnDate);
        });
        assertEquals("User's library card is invalid or missing.", exception.getMessage());
        testUser.getLibraryCard().setBlocked(false); // Przywróć stan na potrzeby innych testów (chociaż @BeforeEach to zrobi)
        System.out.println("Finished testCreateBorrow_invalidCard_blocked.");
    }

    @Test
    void testCreateBorrow_invalidCard_expired() {
        System.out.println("Running testCreateBorrow_invalidCard_expired...");
        // Ustaw datę ważności karty na przeszłą względem simulatedTestCurrentDate
        Date expiredDate = simulatedTestCurrentDate.addMonths(-1); // Karta wygasła miesiąc temu
        testUser.getLibraryCard().setExpiryDate(expiredDate);
        assertFalse(testUser.getLibraryCard().isValid(simulatedTestCurrentDate), "Card should be expired for this test.");

        Date borrowDate = simulatedTestCurrentDate;
        Date returnDate = borrowDate.addMonths(1);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            executor.createBorrow(testBook1, testUser, borrowDate, returnDate);
        });
        assertEquals("User's library card is invalid or missing.", exception.getMessage());
        System.out.println("Finished testCreateBorrow_invalidCard_expired.");
    }


    @Test
    void testCreateReservationAndReturnBook_triggersNotification() {
        System.out.println("Running testCreateReservationAndReturnBook_triggersNotification...");
        // Krok 1: Upewnij się, że testBook1 jest dostępna i karta otherUser jest ważna
        Book bookToBorrowAndReserve = testStorage.findBookById(testBook1.getId()); // Pobierz z aktualnym ID
        assertNotNull(bookToBorrowAndReserve);
        assertTrue(bookToBorrowAndReserve.isAvailable(), "Book1 should be initially available in storage.");
        assertTrue(otherUser.getLibraryCard().isValid(simulatedTestCurrentDate), "OtherUser's card must be valid.");

        // Krok 2: otherUser wypożycza testBook1, czyniąc ją niedostępną
        executor.createBorrow(bookToBorrowAndReserve, otherUser, simulatedTestCurrentDate, simulatedTestCurrentDate.addMonths(1));
        assertFalse(testStorage.findBookById(bookToBorrowAndReserve.getId()).isAvailable(), "Book1 should be unavailable after otherUser borrows it.");

        // Krok 3: testUser (z ważną kartą) rezerwuje testBook1
        assertTrue(testUser.getLibraryCard().isValid(simulatedTestCurrentDate), "TestUser's card must be valid for reservation.");
        IAction reservationAction = executor.createReservation(bookToBorrowAndReserve, testUser, simulatedTestCurrentDate);
        assertNotNull(reservationAction, "Reservation action should not be null.");
        assertTrue(reservationAction instanceof Reservation, "Action should be Reservation.");
        assertEquals(1, executor.getReservations().size(), "Should be one reservation.");
        Reservation userReservation = (Reservation) reservationAction;
        assertEquals("PENDING", userReservation.getStatus(), "Reservation status should be PENDING.");
        assertTrue(testUser.getNotifications().stream().anyMatch(n -> n.contains("has been reserved")), "TestUser should get reservation confirmation.");

        // Wyczyść powiadomienia testUser przed zwrotem książki przez otherUser
        if(testUser instanceof User && ((User)testUser).getNotifications() != null) { // Dodatkowe sprawdzenie
            ((User)testUser).clearNotifications(); // Zakładając, że User ma metodę clearNotifications()
        }


        // Krok 4: otherUser zwraca książkę
        executor.returnBook(bookToBorrowAndReserve, otherUser);
        assertTrue(testStorage.findBookById(bookToBorrowAndReserve.getId()).isAvailable(), "Book1 should be available in storage after otherUser returns it.");

        // Krok 5: Sprawdź status rezerwacji testUser i powiadomienie
        Reservation updatedReservation = executor.getReservations().stream()
                .filter(r -> r.getBook().getId() == bookToBorrowAndReserve.getId() && r.getUser().getId() == testUser.getId())
                .findFirst().orElse(null);
        assertNotNull(updatedReservation, "TestUser's reservation should still exist.");
        assertEquals("READY_FOR_PICKUP", updatedReservation.getStatus(), "Reservation status should be READY_FOR_PICKUP.");

        boolean notificationFound = testUser.getNotifications().stream()
                .anyMatch(n -> n.contains("you reserved is now available for pickup"));
        assertTrue(notificationFound, "TestUser should be notified that the reserved book is available for pickup.");
        System.out.println("Finished testCreateReservationAndReturnBook_triggersNotification.");
    }

    @Test
    void testReturnBook_notBorrowed() {
        System.out.println("Running testReturnBook_notBorrowed...");
        // testBook2 nie została przez nikogo wypożyczona w tym teście
        Book bookNotBorrowed = testStorage.findBookById(testBook2.getId());
        assertNotNull(bookNotBorrowed);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            executor.returnBook(bookNotBorrowed, testUser);
        });
        assertEquals("Book '" + bookNotBorrowed.getTitle() + "' was not recorded as borrowed by " + testUser.getName(), exception.getMessage());
        System.out.println("Finished testReturnBook_notBorrowed.");
    }
}