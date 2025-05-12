package com.librarysystem;

public class Borrow implements IAction {
    private Date borrowDate;
    private Date returnDate;
    private Book book;
    private User user;

    public Borrow(Book book, Date borrowDate, Date returnDate, User user) {
        this.book = book;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.user = user;
    }

    public Date getBorrowDate() { return borrowDate; }
    public Date getReturnDate() { return returnDate; }
    public Book getBook() { return book; }
    public User getUser() { return user; }


    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    @Override
    public void accept(Executor executor) {
        executor.executeBorrow(this);
    }

    public boolean isOverdue(Date currentDate) {
        return currentDate.isAfter(returnDate);
    }
}