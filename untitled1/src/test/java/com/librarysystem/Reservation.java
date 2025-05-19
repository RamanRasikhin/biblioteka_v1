package com.librarysystem;

public class Reservation implements IAction {
    private Date reservationDate;
    private String status;
    private Book book;
    private INotify notifyTarget;
    private User user;

    public Reservation(Book book, Date reservationDate, String status, User user) {
        this.book = book;
        this.reservationDate = reservationDate;
        this.status = status;
        this.user = user;
        this.notifyTarget = user;
    }

    public Date getReservationDate() { return reservationDate; }
    public String getStatus() { return status; }
    public Book getBook() { return book; }
    public INotify getNotifyTarget() { return notifyTarget; }
    public User getUser() { return user; }


    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void accept(Executor executor) {
        executor.executeReservation(this);
    }
}