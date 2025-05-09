// ============== File: com/librarysystem/Borrow.java ==============
package com.librarysystem;

// Corresponds to UML Borrow
public class Borrow implements IAction { // CHANGED: IPerform to IAction
    private Date borrowDate;
    private Date returnDate;
    private Book book;
    private User user; // Added to link back to the user

    public Borrow(Book book, Date borrowDate, Date returnDate, User user) {
        this.book = book;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.user = user;
    }

    // Getters from UML (and C++)
    public Date getBorrowDate() { return borrowDate; }
    public Date getReturnDate() { return returnDate; }
    public Book getBook() { return book; }
    public User getUser() { return user; }


    // Setters if needed, e.g., for extending return date
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    // Method from UML (Visitor pattern)
    @Override
    public void accept(Executor executor) {
        executor.executeBorrow(this);
    }

    // from C++ Wypozyczenie.czyPrzetrzymana
    public boolean isOverdue(Date currentDate) {
        return currentDate.isAfter(returnDate);
    }
}