package com.librarysystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class AccessManagerTest {

    private static final String TEST_USERS_FILE_PATH = "test_users.csv";
    private AccessManager accessManager;

    @BeforeEach
    void setUp() throws IOException {
        File testFile = new File(TEST_USERS_FILE_PATH);
        if (testFile.exists()) {
            if (!testFile.delete()) {
                System.err.println("Warning: Could not delete test users file: " + TEST_USERS_FILE_PATH);
            }
        }
        accessManager = new AccessManager(TEST_USERS_FILE_PATH);
    }

    @AfterEach
    void tearDown() throws IOException {
        File testFile = new File(TEST_USERS_FILE_PATH);
        if (testFile.exists()) {
            testFile.delete();
        }
        accessManager = null;
    }

    @Test
    void testCreateUserAndLogin() {
        System.out.println("Running testCreateUserAndLogin...");
        Date expiry = new Date(2025, 12, 31);
        accessManager.createUser("John", "Doe", "john.doe@example.com", "READER", "password123", 5, expiry);

        User loggedInUser = accessManager.login("john.doe@example.com", "password123");
        assertNotNull(loggedInUser, "John Doe should be able to login");
        assertEquals("John", loggedInUser.getName());
        assertEquals("READER", loggedInUser.getRole());

        User nonExistentUser = accessManager.login("jane.doe@example.com", "wrongpassword");
        assertNull(nonExistentUser, "Non-existent user should not login");

        User wrongPasswordUser = accessManager.login("john.doe@example.com", "wrongpassword");
        assertNull(wrongPasswordUser, "User with wrong password should not login");
        System.out.println("Finished testCreateUserAndLogin.");
    }

    @Test
    void testCreateUser_duplicateEmail() {
        System.out.println("Running testCreateUser_duplicateEmail...");
        Date expiry = new Date(2025, 12, 31);
        accessManager.createUser("Jane", "Doe", "jane.doe@example.com", "READER", "password123", 5, expiry);
        assertThrows(IllegalArgumentException.class, () -> {
            accessManager.createUser("Janet", "Doe", "jane.doe@example.com", "LIBRARIAN", "securepass", 10, expiry);
        }, "Should throw IllegalArgumentException for duplicate email");
        System.out.println("Finished testCreateUser_duplicateEmail.");
    }

    @Test
    void testFindUserById() {
        System.out.println("Running testFindUserById...");
        Date expiry = new Date(2026, 1, 1);
        accessManager.createUser("Alice", "Smith", "alice@example.com", "LIBRARIAN", "alicepass", 10, expiry);
        User createdUser = accessManager.login("alice@example.com", "alicepass");
        assertNotNull(createdUser, "Alice should be created and logged in");

        User foundUser = accessManager.findUserById(createdUser.getId());
        assertNotNull(foundUser, "Alice should be found by ID");
        assertEquals("Alice", foundUser.getName());

        User notFoundUser = accessManager.findUserById(999); 
        assertNull(notFoundUser, "User with ID 999 should not be found");
        System.out.println("Finished testFindUserById.");
    }

    @Test
    void testPersistence() throws IOException {
        System.out.println("Running testPersistence...");
        Date expiry1 = new Date(2025, 1, 1);
        accessManager.createUser("Test", "User1", "test1@example.com", "READER", "pass1", 3, expiry1);
        User originalUser = accessManager.login("test1@example.com", "pass1");
        assertNotNull(originalUser, "Original user should be created");
        int originalUser1Id = originalUser.getId();

        AccessManager newAccessManager = new AccessManager(TEST_USERS_FILE_PATH);

        User reloadedUser1 = newAccessManager.login("test1@example.com", "pass1");
        assertNotNull(reloadedUser1, "User1 powinien zostać wczytany z pliku przez newAccessManager");
        assertEquals(originalUser1Id, reloadedUser1.getId());
        assertEquals("Test", reloadedUser1.getName());
        assertEquals(3, reloadedUser1.getBookLimit());

        assertNotNull(reloadedUser1.getLibraryCard());
        assertTrue(reloadedUser1.getLibraryCard().getExpiryDate().isEqual(expiry1));
        assertFalse(reloadedUser1.getLibraryCard().isBlocked());
        System.out.println("Finished testPersistence.");
    }

    @Test
    void testRemoveUser() throws IOException {
        System.out.println("Running testRemoveUser...");
        Date expiry = new Date(2025, 12, 31);
        String emailToRemove = "remove.me@example.com";
        accessManager.createUser("Remove", "Me", emailToRemove, "READER", "password123", 5, expiry);

        User userBeforeRemove = accessManager.login(emailToRemove, "password123");
        assertNotNull(userBeforeRemove, "User to be removed should exist before removal");

        accessManager.removeUser(emailToRemove);

        User userAfterRemove = accessManager.login(emailToRemove, "password123");
        assertNull(userAfterRemove, "User should not be found after removal from current AccessManager");

        AccessManager freshManager = new AccessManager(TEST_USERS_FILE_PATH);
        User userAfterRemoveFromFile = freshManager.login(emailToRemove, "password123");
        assertNull(userAfterRemoveFromFile, "User should not be found in a new AccessManager instance after removal");
        System.out.println("Finished testRemoveUser.");
    }
}
