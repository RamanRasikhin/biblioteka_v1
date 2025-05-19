package com.librarysystem;

import java.util.ArrayList;
import java.util.List;

public class User implements com.librarysystem.INotify {
    private String name;
    private String surname;
    private String email;
    private String role;
    private com.librarysystem.LibraryCard libraryCard;
    private String password;
    private List<String> notifications;

    private int id;
    private int bookLimit;


    public User(int id, String name, String surname, String email, String role, String password, int bookLimit, Date cardExpiryDate) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.role = role;
        this.password = password;
        this.notifications = new ArrayList<>();
        this.bookLimit = bookLimit > 0 ? bookLimit : 5;
        this.libraryCard = new LibraryCard(id, cardExpiryDate);
    }

    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public com.librarysystem.LibraryCard getLibraryCard() { return libraryCard; }
    public String getPassword() { return password; }
    public List<String> getNotifications() { return new ArrayList<>(notifications); }

    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setPassword(String password) { this.password = password; }

    public int getId() { return id; }
    public int getBookLimit() { return bookLimit; }
    public void setBookLimit(int bookLimit) { this.bookLimit = bookLimit; }


    public void accessSystem(Gateway gateway) {
        System.out.println(name + " is accessing the system via Gateway.");
    }

    public boolean login(String passwordAttempt) {
        return this.password.equals(passwordAttempt);
    }

    @Override
    public void recieveMessage(String message) {
        this.notifications.add(message);
        System.out.println("Notification for " + name + ": " + message);
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", surname=" + surname + ", email=" + email
                + ", role=" + role + ", bookLimit=" + bookLimit
                + (libraryCard != null ? ", cardValidUntil=" + libraryCard.getExpiryDate() + ", cardBlocked=" + libraryCard.isBlocked() : ", NoCard")
                + "]";
    }
    public void clearNotifications() {
        if (this.notifications != null) {
            this.notifications.clear();
        }
    }
}