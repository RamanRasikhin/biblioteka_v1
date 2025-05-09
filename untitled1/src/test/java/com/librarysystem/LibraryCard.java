package com.librarysystem;

import java.util.ArrayList;
import java.util.List;

// Corresponds to UML LibraryCard
public class LibraryCard {
    private List<com.librarysystem.IAction> commands; // UML: commands: vector<Action>
    private com.librarysystem.Date expiryDate; // From C++ KartaBiblioteczna
    private boolean blocked;   // From C++ KartaBiblioteczna
    private int cardId; // Associated with User or unique

    public LibraryCard(int cardId, com.librarysystem.Date expiryDate) {
        this.cardId = cardId;
        this.commands = new ArrayList<>();
        this.expiryDate = expiryDate;
        this.blocked = false; // Default not blocked
    }

    // Methods from UML
    public void addAction(com.librarysystem.IAction action) { // UML: add_action(perform: IPerform): void
        this.commands.add(action);
    }

    // Adapting from C++ KartaBiblioteczna
    public boolean isValid(com.librarysystem.Date currentDate) {
        if (blocked) return false;
        return currentDate.isBefore(expiryDate) || currentDate.isEqual(expiryDate);
    }

    public com.librarysystem.Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(com.librarysystem.Date expiryDate) { this.expiryDate = expiryDate; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public int getCardId() { return cardId; }

    public List<com.librarysystem.IAction> getCommands() { return new ArrayList<>(commands); } // Return copy

    public void addCommand(com.librarysystem.IAction command) { // Same as addAction, more direct name
        this.commands.add(command);
    }
}
