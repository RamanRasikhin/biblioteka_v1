package com.librarysystem;

public class Borrow implements IAction {
    private Date borrowDate;
    private Date returnDate;
    private Book book;
    private User user;
    private boolean reminderSentForThisPeriod;

    public Borrow(Book book, Date borrowDate, Date returnDate, User user) {
        this.book = book;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.user = user;
        this.reminderSentForThisPeriod = false;
    }

    public Date getBorrowDate() { return borrowDate; }
    public Date getReturnDate() { return returnDate; }
    public Book getBook() { return book; }
    public User getUser() { return user; }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
        this.reminderSentForThisPeriod = false;
    }

    @Override
    public void accept(Executor executor) {
        executor.executeBorrow(this);
    }

    public boolean isOverdue(Date currentDate) {
        if (currentDate == null || returnDate == null) return false;
        return currentDate.isAfter(returnDate);
    }

    public boolean isReminderSentForThisPeriod() {
        return reminderSentForThisPeriod;
    }

    public void setReminderSentForThisPeriod(boolean reminderSentForThisPeriod) {
        this.reminderSentForThisPeriod = reminderSentForThisPeriod;
    }
}