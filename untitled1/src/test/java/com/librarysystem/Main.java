package com.librarysystem;

import java.util.List;
import java.util.Scanner;
import java.util.InputMismatchException;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static com.librarysystem.AccessManager accessManager;
    private static com.librarysystem.Gateway gateway;
    private static com.librarysystem.Storage storage;
    private static com.librarysystem.LookupArray lookupArray;

    private static com.librarysystem.User currentUser = null;
    private static com.librarysystem.Date simulatedCurrentDate;
    private static final int REMINDER_DAYS_IN_ADVANCE = 7;

    public static void main(String[] args) {
        storage = new com.librarysystem.Storage();
        lookupArray = new com.librarysystem.LookupArray(storage);
        accessManager = new com.librarysystem.AccessManager();
        gateway = new com.librarysystem.Gateway(lookupArray, storage);
        simulatedCurrentDate = com.librarysystem.Date.getCurrentDate();

        System.out.println("Library System Initialized.");
        System.out.println("Simulated Date: " + simulatedCurrentDate.toString());

        if (accessManager.findUserByEmail("admin@library.com") == null && accessManager.getAllUsers().isEmpty()) {
            try {
                accessManager.createUser("Admin", "User", "admin@library.com", "LIBRARIAN", "admin123",
                        10, simulatedCurrentDate.addMonths(24));
            } catch (IllegalArgumentException e) {
                System.err.println("Could not create default admin: " + e.getMessage());
            }
        }
        gateway.getExecutor().checkAndNotifyForUpcomingReturns(simulatedCurrentDate, REMINDER_DAYS_IN_ADVANCE);

        mainMenu();
        scanner.close();
    }

    private static void mainMenu() {
        int choice = 0;
        do {
            if (currentUser != null) {
                gateway.getExecutor().checkAndNotifyForUpcomingReturns(simulatedCurrentDate, REMINDER_DAYS_IN_ADVANCE);
            }

            System.out.println("\n--- LIBRARY MAIN MENU (Simulated Date: " + simulatedCurrentDate.toString() + ") ---");
            if (currentUser == null) {
                System.out.println("1. Login");
                System.out.println("2. Register (New Reader)");
            } else {
                System.out.println("Logged in as: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
                System.out.println("1. Logout");
                if ("READER".equals(currentUser.getRole())) {
                    System.out.println("2. Reader Menu");
                } else if ("LIBRARIAN".equals(currentUser.getRole())) {
                    System.out.println("2. Librarian Menu");
                }
            }
            System.out.println("3. Simulate Time");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            choice = getIntInput();

            if (currentUser == null) {
                switch (choice) {
                    case 1: loginUser(); break;
                    case 2: registerReader(); break;
                    case 3: simulateTimeMenu(); break;
                    case 4: System.out.println("Exiting..."); break;
                    default: System.out.println("Invalid choice.");
                }
            } else {
                switch (choice) {
                    case 1: logoutUser(); break;
                    case 2:
                        if ("READER".equals(currentUser.getRole())) readerMenu();
                        else if ("LIBRARIAN".equals(currentUser.getRole())) librarianMenu();
                        break;
                    case 3: simulateTimeMenu(); break;
                    case 4: System.out.println("Exiting..."); break;
                    default: System.out.println("Invalid choice.");
                }
            }
        } while (choice != 4);
    }

    private static void loginUser() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        currentUser = accessManager.login(email, password);
        if (currentUser != null) {
            System.out.println("Login successful for " + currentUser.getName());
            gateway.getExecutor().checkAndNotifyForUpcomingReturns(simulatedCurrentDate, REMINDER_DAYS_IN_ADVANCE);
            if(!currentUser.getNotifications().isEmpty()){
                System.out.println("You have new notifications! Check them in the Reader Menu.");
            }
        } else {
            System.out.println("Login failed. Please check credentials.");
        }
    }

    private static void simulateTimeMenu() {
        int choice;
        do {
            System.out.println("\n--- SIMULATE TIME ---");
            System.out.println("Current Simulated Date: " + simulatedCurrentDate.toString());
            System.out.println("1. Add Months");
            System.out.println("2. Subtract Months");
            System.out.println("3. Add Days");
            System.out.println("4. Subtract Days");
            System.out.println("5. Reset to Real Date");
            System.out.println("6. Back to Main Menu");
            System.out.print("Enter choice: ");
            choice = getIntInput();

            Date oldDate = simulatedCurrentDate;

            switch (choice) {
                case 1:
                    System.out.print("Months to add: "); int addM = getIntInput();
                    simulatedCurrentDate = simulatedCurrentDate.addMonths(addM);
                    break;
                case 2:
                    System.out.print("Months to subtract: "); int subM = getIntInput();
                    simulatedCurrentDate = simulatedCurrentDate.addMonths(-subM);
                    break;
                case 3:
                    System.out.print("Days to add: "); int addD = getIntInput();
                    simulatedCurrentDate = simulatedCurrentDate.plusDays(addD);
                    break;
                case 4:
                    System.out.print("Days to subtract: "); int subD = getIntInput();
                    simulatedCurrentDate = simulatedCurrentDate.minusDays(subD);
                    break;
                case 5:
                    simulatedCurrentDate = com.librarysystem.Date.getCurrentDate();
                    break;
                case 6: break;
                default: System.out.println("Invalid choice.");
            }
            System.out.println("New Simulated Date: " + simulatedCurrentDate.toString());

            if (!simulatedCurrentDate.isEqual(oldDate)) {
                for (Borrow borrow : gateway.getExecutor().getBorrows()) {
                    borrow.setReminderSentForThisPeriod(false);
                }
            }
            gateway.getExecutor().checkAndNotifyForUpcomingReturns(simulatedCurrentDate, REMINDER_DAYS_IN_ADVANCE);

        } while (choice != 6);
    }

    private static void viewMyReservations() {
        List<com.librarysystem.Reservation> reservations = gateway.getUserActiveReservations(currentUser);
        if (reservations.isEmpty()) {
            System.out.println("You have no active reservations.");
            return;
        }
        System.out.println("\n--- MY ACTIVE RESERVATIONS ---");
        for (com.librarysystem.Reservation res : reservations) {
            System.out.println("Title: " + res.getBook().getTitle() +
                    ", Author: " + res.getBook().getAuthor() +
                    ", Reserved On: " + res.getReservationDate() +
                    ", Status: " + res.getStatus());
        }
    }

    private static void viewAllReservations() {
        int choice;
        System.out.println("\n--- VIEW ALL RESERVATIONS (LIBRARIAN) ---");
        System.out.println("1. View only active (PENDING, READY_FOR_PICKUP)");
        System.out.println("2. View full history (including FULFILLED, CANCELLED)");
        System.out.print("Enter choice: ");
        choice = getIntInput();

        List<com.librarysystem.Reservation> reservations;
        if (choice == 1) {
            reservations = gateway.getAllActiveReservations();
            System.out.println("\n--- ALL ACTIVE RESERVATIONS ---");
        } else if (choice == 2) {
            reservations = gateway.getAllReservationsHistory();
            System.out.println("\n--- FULL RESERVATION HISTORY ---");
        } else {
            System.out.println("Invalid choice. Returning to menu.");
            return;
        }

        if (reservations.isEmpty()) {
            System.out.println("No reservations found for the selected criteria.");
            return;
        }
        for (com.librarysystem.Reservation r : reservations) {
            System.out.println("Book: '" + r.getBook().getTitle() + "' (ID: " + r.getBook().getId() + ")" +
                    ", User: " + r.getUser().getName() + " (ID: " + r.getUser().getId() + ")" +
                    ", Reserved: " + r.getReservationDate() + ", Status: " + r.getStatus());
        }
    }

    private static void registerReader() {
        System.out.print("Enter name: "); String name = scanner.nextLine();
        System.out.print("Enter surname: "); String surname = scanner.nextLine();
        System.out.print("Enter email: "); String email = scanner.nextLine();
        System.out.print("Enter password: "); String password = scanner.nextLine();
        try {
            accessManager.createUser(name, surname, email, "READER", password, 5, simulatedCurrentDate.addMonths(12));
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private static void logoutUser() {
        if (currentUser != null) {
            System.out.println(currentUser.getName() + " logged out.");
            currentUser = null;
        } else {
            System.out.println("No user currently logged in.");
        }
    }

    private static void readerMenu() {
        int choice;
        do {
            gateway.getExecutor().checkAndNotifyForUpcomingReturns(simulatedCurrentDate, REMINDER_DAYS_IN_ADVANCE);

            System.out.println("\n--- READER MENU (" + currentUser.getName() + ") ---");
            System.out.println("1. Borrow Book");
            System.out.println("2. Return Book");
            System.out.println("3. Reserve Book");
            System.out.println("4. View My Borrows");
            System.out.println("5. View My Active Reservations");
            System.out.println("6. Search Books");
            System.out.println("7. List Available Books");
            System.out.println("8. View My Notifications");
            System.out.println("9. View My Card Details");
            System.out.println("10. Back to Main Menu");
            System.out.print("Enter choice: ");
            choice = getIntInput();

            try {
                switch (choice) {
                    case 1: borrowBook(); break;
                    case 2: returnBook(); break;
                    case 3: reserveBook(); break;
                    case 4: viewMyBorrows(); break;
                    case 5: viewMyReservations(); break;
                    case 6: searchBooks(); break;
                    case 7: listAvailableBooks(); break;
                    case 8: viewMyNotifications(); break;
                    case 9: viewMyCard(); break;
                    case 10: break;
                    default: System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.err.println("Error in Reader Menu: " + e.getMessage());
            }
        } while (choice != 10);
    }

    private static void listAvailableBooks() {
        List<com.librarysystem.Book> books = gateway.listAvailableBooks();
        if (books.isEmpty()) {
            System.out.println("No books currently available in the library.");
            return;
        }
        System.out.println("\n--- AVAILABLE BOOKS ---");
        for (com.librarysystem.Book book : books) {
            System.out.println("ID: " + book.getId() + ", Title: " + book.getTitle() +
                    ", Author: " + book.getAuthor() + ", Genre: " + book.getGenre());
        }
    }

    private static void viewMyCard() {
        com.librarysystem.LibraryCard card = currentUser.getLibraryCard();
        if (card != null) {
            System.out.println("\n--- MY LIBRARY CARD ---");
            System.out.println("Card ID: " + card.getCardId());
            System.out.println("Expires on: " + card.getExpiryDate());
            System.out.println("Is Blocked: " + card.isBlocked());
            System.out.println("Is Valid (as of " + simulatedCurrentDate.toString() + "): " + card.isValid(simulatedCurrentDate));
            System.out.println("Book Limit: " + currentUser.getBookLimit());
        } else {
            System.out.println("You do not have a library card associated with your account.");
        }
    }

    private static void borrowBook() {
        System.out.print("Enter Book ID to borrow: ");
        int bookId = getIntInput();
        com.librarysystem.Book book = gateway.findBookById(bookId);
        if (book == null) {
            System.out.println("Book with ID " + bookId + " not found.");
            return;
        }
        try {
            gateway.createBorrow(book, currentUser, simulatedCurrentDate, simulatedCurrentDate.addMonths(1));
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Could not borrow book: " + e.getMessage());
        }
    }

    private static void returnBook() {
        System.out.print("Enter Book ID to return: ");
        int bookId = getIntInput();
        com.librarysystem.Book book = gateway.findBookById(bookId);
        if (book == null) {
            System.out.println("Book with ID " + bookId + " not found.");
            return;
        }
        try {
            gateway.returnBook(book, currentUser);
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Could not return book: " + e.getMessage());
        }
    }

    private static void reserveBook() {
        System.out.print("Enter Book ID to reserve: ");
        int bookId = getIntInput();
        com.librarysystem.Book book = gateway.findBookById(bookId);
        if (book == null) {
            System.out.println("Book with ID " + bookId + " not found.");
            return;
        }
        try {
            gateway.createReservation(book, currentUser, simulatedCurrentDate);
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Could not reserve book: " + e.getMessage());
        }
    }

    private static void viewMyBorrows() {
        List<com.librarysystem.Borrow> borrows = gateway.getUserBorrows(currentUser);
        if (borrows.isEmpty()) {
            System.out.println("You have no borrowed books.");
            return;
        }
        System.out.println("\n--- MY BORROWED BOOKS ---");
        for (com.librarysystem.Borrow borrow : borrows) {
            System.out.println("Title: " + borrow.getBook().getTitle() +
                    ", Author: " + borrow.getBook().getAuthor() +
                    ", Borrowed On: " + borrow.getBorrowDate() +
                    ", Return By: " + borrow.getReturnDate() +
                    (borrow.isOverdue(simulatedCurrentDate) ? " (OVERDUE)" : "") +
                    (borrow.isReminderSentForThisPeriod() ? " (Reminder Sent)" : "")
            );
        }
    }

    private static void searchBooks() {
        System.out.print("Enter search term (title, author, ISBN, genre) or leave empty to list all: ");
        String term = scanner.nextLine();
        if (term.trim().isEmpty()) {
            System.out.println("Listing all books:");
            viewAllBooksLibrarian();
            return;
        }
        List<com.librarysystem.Book> books = gateway.searchBooks(term);
        if (books.isEmpty()) {
            System.out.println("No books found matching your criteria: '" + term + "'");
            return;
        }
        System.out.println("\n--- SEARCH RESULTS ---");
        for (com.librarysystem.Book book : books) {
            System.out.println("ID: " + book.getId() + ", Title: " + book.getTitle() +
                    ", Author: " + book.getAuthor() + ", Genre: " + book.getGenre() +
                    ", ISBN: " + book.getIsbn() + ", Available: " + book.isAvailable());
        }
    }

    private static void viewMyNotifications() {
        List<String> notifications = currentUser.getNotifications();
        if (notifications.isEmpty()) {
            System.out.println("No new notifications.");
            return;
        }
        System.out.println("\n--- MY NOTIFICATIONS ---");
        for (String notification : notifications) {
            System.out.println("- " + notification);
        }
        currentUser.clearNotifications();
        System.out.println("(Notifications cleared after viewing)");
    }

    private static void librarianMenu() {
        int choice;
        do {
            gateway.getExecutor().checkAndNotifyForUpcomingReturns(simulatedCurrentDate, REMINDER_DAYS_IN_ADVANCE);

            System.out.println("\n--- LIBRARIAN MENU (" + currentUser.getName() + ") ---");
            System.out.println("1. Add Book");
            System.out.println("2. Remove Book");
            System.out.println("3. View All Books (Full Details)");
            System.out.println("4. Search Books");
            System.out.println("5. Manage Users");
            System.out.println("6. View All Borrows");
            System.out.println("7. View All Reservations (Active/History)");
            System.out.println("8. Back to Main Menu");
            System.out.print("Enter choice: ");
            choice = getIntInput();

            try {
                switch (choice) {
                    case 1: addBook(); break;
                    case 2: removeBook(); break;
                    case 3: viewAllBooksLibrarian(); break;
                    case 4: searchBooks(); break;
                    case 5: manageUsersMenu(); break;
                    case 6: viewAllBorrows(); break;
                    case 7: viewAllReservations(); break;
                    case 8: break;
                    default: System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.err.println("Librarian Menu Error: " + e.getMessage());
            }
        } while (choice != 8);
    }

    private static void manageUsersMenu() {
        int choice;
        do {
            System.out.println("\n--- MANAGE USERS ---");
            System.out.println("1. Add New User (Reader/Librarian)");
            System.out.println("2. View All Users");
            System.out.println("3. Set User Book Limit");
            System.out.println("4. Extend User Card Expiry");
            System.out.println("5. Block/Unblock User Card");
            System.out.println("6. Remove User");
            System.out.println("7. Back to Librarian Menu");
            System.out.print("Enter choice: ");
            choice = getIntInput();

            switch(choice) {
                case 1: addNewUserByAdmin(); break;
                case 2: viewAllUsers(); break;
                case 3: setUserBookLimit(); break;
                case 4: extendUserCardExpiry(); break;
                case 5: toggleBlockUserCard(); break;
                case 6: removeUserByAdmin(); break;
                case 7: break;
                default: System.out.println("Invalid choice.");
            }
        } while (choice != 7);
    }

    private static void addNewUserByAdmin() {
        System.out.print("Enter name: "); String name = scanner.nextLine();
        System.out.print("Enter surname: "); String surname = scanner.nextLine();
        System.out.print("Enter email: "); String email = scanner.nextLine();
        System.out.print("Enter password: "); String password = scanner.nextLine();
        System.out.print("Enter role (READER/LIBRARIAN): "); String role = scanner.nextLine().toUpperCase();
        if (!role.equals("READER") && !role.equals("LIBRARIAN")) {
            System.out.println("Invalid role. Must be READER or LIBRARIAN."); return;
        }
        System.out.print("Enter book limit: "); int limit = getIntInput();
        System.out.print("Card expiry in months from now (e.g., 12): "); int expiryMonths = getIntInput();

        try {
            accessManager.createUser(name, surname, email, role, password, limit, simulatedCurrentDate.addMonths(expiryMonths));
        } catch (IllegalArgumentException e) {
            System.out.println("Failed to create user: " + e.getMessage());
        }
    }

    private static void viewAllUsers() {
        List<com.librarysystem.User> users = accessManager.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users in the system."); return;
        }
        System.out.println("\n--- ALL USERS ---");
        for (com.librarysystem.User u : users) {
            System.out.println(u.toString() + ", Card Valid Now: " + (u.getLibraryCard() != null ? u.getLibraryCard().isValid(simulatedCurrentDate) : "N/A"));
        }
    }

    private static void setUserBookLimit() {
        System.out.print("Enter User ID to modify limit: "); int userId = getIntInput();
        com.librarysystem.User user = accessManager.findUserById(userId);
        if (user == null) { System.out.println("User with ID " + userId + " not found."); return; }
        System.out.print("Current book limit for " + user.getName() + " is " + user.getBookLimit() + ". Enter new book limit: ");
        int newLimit = getIntInput();
        if (newLimit <=0) { System.out.println("Limit must be a positive number."); return; }
        user.setBookLimit(newLimit);
        accessManager.saveUsers();
        System.out.println("Book limit for " + user.getName() + " (ID: " + userId + ") updated to " + newLimit);
    }

    private static void extendUserCardExpiry() {
        System.out.print("Enter User ID to extend card: "); int userId = getIntInput();
        com.librarysystem.User user = accessManager.findUserById(userId);
        if (user == null || user.getLibraryCard() == null) { System.out.println("User with ID " + userId + " or their card not found."); return; }
        System.out.print("Current card expiry for " + user.getName() + " is " + user.getLibraryCard().getExpiryDate() + ". Months to extend expiry by: ");
        int months = getIntInput();
        if (months <=0) { System.out.println("Months to extend must be a positive number."); return; }
        com.librarysystem.Date newExpiry = user.getLibraryCard().getExpiryDate().addMonths(months);
        user.getLibraryCard().setExpiryDate(newExpiry);
        accessManager.saveUsers();
        System.out.println("Card expiry for " + user.getName() + " (ID: " + userId + ") extended to " + newExpiry);
    }

    private static void toggleBlockUserCard() {
        System.out.print("Enter User ID to block/unblock card: "); int userId = getIntInput();
        com.librarysystem.User user = accessManager.findUserById(userId);
        if (user == null || user.getLibraryCard() == null) { System.out.println("User with ID " + userId + " or their card not found."); return; }
        boolean currentStatus = user.getLibraryCard().isBlocked();
        user.getLibraryCard().setBlocked(!currentStatus);
        accessManager.saveUsers();
        System.out.println("Card for " + user.getName() + " (ID: " + userId + ") is now " + (!currentStatus ? "BLOCKED" : "UNBLOCKED"));
    }

    private static void removeUserByAdmin() {
        System.out.print("Enter email of user to remove: "); String email = scanner.nextLine();
        com.librarysystem.User userToRemove = accessManager.findUserByEmail(email);

        if (userToRemove == null) {
            System.out.println("User with email " + email + " not found.");
            return;
        }

        if (!gateway.getUserBorrows(userToRemove).isEmpty()) {
            System.out.println("Cannot remove user " + userToRemove.getName() + " (ID: " + userToRemove.getId() + "). They have active borrows.");
            return;
        }
        boolean hasActiveReservations = gateway.getUserActiveReservations(userToRemove).stream()
                .anyMatch(r -> "PENDING".equals(r.getStatus()) || "READY_FOR_PICKUP".equals(r.getStatus()));
        if (hasActiveReservations) {
            System.out.println("Cannot remove user " + userToRemove.getName() + " (ID: " + userToRemove.getId() + "). They have active reservations.");
            return;
        }
        accessManager.removeUser(email);
    }

    private static void addBook() {
        System.out.print("Enter title: "); String title = scanner.nextLine();
        System.out.print("Enter author: "); String author = scanner.nextLine();
        System.out.print("Enter genre: "); String genre = scanner.nextLine();
        System.out.print("Enter description: "); String description = scanner.nextLine();
        System.out.print("Enter ISBN: "); String isbn = scanner.nextLine();

        com.librarysystem.Book newBook = new com.librarysystem.Book(-1, title, author, genre, description, isbn, true);
        gateway.addBook(newBook);
    }

    private static void removeBook() {
        System.out.print("Enter ID of book to remove: ");
        int bookId = getIntInput();
        gateway.removeBookById(bookId);
    }

    private static void viewAllBooksLibrarian() {
        List<com.librarysystem.Book> books = storage.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books in the system.");
            return;
        }
        System.out.println("\n--- ALL BOOKS (LIBRARIAN VIEW - FULL DETAILS) ---");
        for (com.librarysystem.Book book : books) {
            System.out.println("ID: " + book.getId() + ", Title: " + book.getTitle() +
                    ", Author: " + book.getAuthor() + ", Genre: " + book.getGenre() + ", ISBN: " + book.getIsbn() +
                    ", Description: " + book.getDescription() + ", Available: " + book.isAvailable());
        }
    }

    private static void viewAllBorrows() {
        List<com.librarysystem.Borrow> borrows = gateway.getAllBorrows(simulatedCurrentDate);
        if (borrows.isEmpty()) {
            System.out.println("No active borrows in the system.");
            return;
        }
        System.out.println("\n--- ALL ACTIVE BORROWS ---");
        for (com.librarysystem.Borrow b : borrows) {
            System.out.println("Book: '" + b.getBook().getTitle() + "' (ID: " + b.getBook().getId() + ")" +
                    ", User: " + b.getUser().getName() + " (ID: " + b.getUser().getId() + ")" +
                    ", Borrowed: " + b.getBorrowDate() + ", Due: " + b.getReturnDate() +
                    (b.isOverdue(simulatedCurrentDate) ? " (OVERDUE)" : "") +
                    (b.isReminderSentForThisPeriod() ? " (Reminder Sent)" : ""));
        }
    }

    private static int getIntInput() {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine();
                return input;
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.nextLine();
            }
        }
    }
}
