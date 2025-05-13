package com.librarysystem;

import java.util.ArrayList;
import java.util.List;

public class LibraryCard {
    private List<com.librarysystem.IAction> commands;
    private com.librarysystem.Date expiryDate;
    private boolean blocked;
    private int cardId;

    public LibraryCard(int cardId, com.librarysystem.Date expiryDate) {
        this.cardId = cardId;
        this.commands = new ArrayList<>();
        this.expiryDate = expiryDate;
        this.blocked = false;
    }

    public void addAction(com.librarysystem.IAction action) {
        this.commands.add(action);
    }

    public boolean isValid(com.librarysystem.Date currentDate) {
        if (blocked) return false;
        return currentDate.isBefore(expiryDate) || currentDate.isEqual(expiryDate);
    }

    public com.librarysystem.Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(com.librarysystem.Date expiryDate) { this.expiryDate = expiryDate; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public int getCardId() { return cardId; }

    public List<com.librarysystem.IAction> getCommands() { return new ArrayList<>(commands); }

    public void addCommand(com.librarysystem.IAction command) {
        this.commands.add(command);
    }
}
