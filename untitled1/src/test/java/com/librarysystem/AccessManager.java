// ============== File: com/librarysystem/AccessManager.java (MODIFIED) ==============
package com.librarysystem;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.*; // For file operations

// Corresponds to UML AccessManager
public class AccessManager {
    private Map<String, com.librarysystem.User> users; // UML: users: map (email as key)
    private String usersFilePath = "users.csv"; // For persistence

    public AccessManager() { this("users.csv"); }
    public AccessManager(String usersFilePath) {
        this.users = new HashMap<>();
        this.usersFilePath = usersFilePath; // Użyj przekazanej ścieżki
        loadUsers(); // Załaduj użytkowników z tej ścieżki
    }

    // Method from UML
    public void createUser(String name, String surname, String email, String role, String password, int bookLimit, com.librarysystem.Date cardExpiryDate) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (users.containsKey(email.toLowerCase())) { // Store and check emails in lowercase for case-insensitivity
            throw new IllegalArgumentException("User with email " + email + " already exists.");
        }
        // Generate ID - simplistic, find max ID + 1
        int newId = users.values().stream().mapToInt(com.librarysystem.User::getId).max().orElse(0) + 1;
        com.librarysystem.User newUser = new com.librarysystem.User(newId, name, surname, email, role, password, bookLimit, cardExpiryDate);
        users.put(email.toLowerCase(), newUser); // Store with lowercase email
        saveUsers();
        System.out.println(role + " user created: " + name + " (ID: " + newId + ", Email: " + email + ")");
    }

    // Method from UML
    public com.librarysystem.User login(String email, String password) {
        if (email == null) return null;
        com.librarysystem.User user = users.get(email.toLowerCase()); // Retrieve with lowercase email
        if (user != null && user.login(password)) {
            System.out.println("Login successful for " + email);
            return user;
        }
        System.out.println("Login failed for " + email);
        return null;
    }

    public com.librarysystem.User findUserById(int id) {
        for (com.librarysystem.User user : users.values()) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    // ***** ADDED METHOD *****
    public com.librarysystem.User findUserByEmail(String email) {
        if (email == null) return null;
        return users.get(email.toLowerCase());
    }


    public List<com.librarysystem.User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public void removeUser(String email) {
        if (email == null) {
            System.out.println("Email for removal cannot be null.");
            return;
        }
        if (users.containsKey(email.toLowerCase())) {
            users.remove(email.toLowerCase());
            saveUsers();
            System.out.println("User " + email + " removed.");
        } else {
            System.out.println("User " + email + " not found for removal.");
        }
    }

    // Persistence (simplified CSV)
    private void loadUsers() {
        File file = new File(usersFilePath);
        if (!file.exists()) {
            System.out.println("Users file ("+ usersFilePath +") not found, starting fresh.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String header = br.readLine(); // Skip header
            if (header == null) { // Empty file
                System.out.println("Users file ("+ usersFilePath +") is empty.");
                return;
            }
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";", -1); // Keep trailing empty strings
                if (parts.length >= 8) { // ID;Name;Surname;Email;Role;Password;BookLimit;CardExpiry;CardBlocked (9 parts)
                    try {
                        int id = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        String surname = parts[2];
                        String email = parts[3];
                        String role = parts[4];
                        String password = parts[5]; // In a real app, hash passwords
                        int bookLimit = Integer.parseInt(parts[6]);
                        com.librarysystem.Date cardExpiry = com.librarysystem.Date.fromString(parts[7]);
                        // CardBlocked is optional, defaults to false if not present or parsing error
                        boolean cardBlocked = (parts.length > 8 && !parts[8].isEmpty()) ? Boolean.parseBoolean(parts[8]) : false;

                        com.librarysystem.User user = new com.librarysystem.User(id, name, surname, email, role, password, bookLimit, cardExpiry);
                        if(user.getLibraryCard() != null) user.getLibraryCard().setBlocked(cardBlocked);
                        users.put(email.toLowerCase(), user); // Store with lowercase email
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed user line (number format error): " + line + " -> " + e.getMessage());
                    } catch (Exception e) { // Catch other potential parsing errors for date, etc.
                        System.err.println("Skipping malformed user line (general error): " + line + " -> " + e.getMessage());
                    }
                } else {
                    if (!line.trim().isEmpty()) System.err.println("Skipping malformed user line (not enough parts): " + line);
                }
            }
            System.out.println("Loaded " + users.size() + " users from " + usersFilePath);
        } catch (IOException e) {
            System.err.println("Error loading users from " + usersFilePath + ": " + e.getMessage());
        }
    }

    public void saveUsers() { // Make public if external trigger needed
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(usersFilePath))) {
            bw.write("ID;Name;Surname;Email;Role;Password;BookLimit;CardExpiry;CardBlocked\n");
            for (com.librarysystem.User user : users.values()) {
                bw.write(user.getId() + ";");
                bw.write(user.getName() + ";");
                bw.write(user.getSurname() + ";");
                bw.write(user.getEmail() + ";"); // Save original case email, but use lowercase for map key
                bw.write(user.getRole() + ";");
                bw.write(user.getPassword() + ";"); // Again, real app: store hashes
                bw.write(user.getBookLimit() + ";");
                if (user.getLibraryCard() != null) {
                    bw.write(user.getLibraryCard().getExpiryDate().toString() + ";");
                    bw.write(user.getLibraryCard().isBlocked() + "\n");
                } else { // Should not happen with current User constructor logic
                    bw.write("N/A;false\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving users to " + usersFilePath + ": " + e.getMessage());
        }
    }
}