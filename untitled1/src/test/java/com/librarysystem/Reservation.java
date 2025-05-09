package com.librarysystem;

// Corresponds to UML Reservation
public class Reservation implements IAction { // CHANGED: IPerform to IAction
    private Date reservationDate;
    private String status; // e.g., "PENDING", "FULFILLED", "CANCELLED"
    private Book book;
    private INotify notifyTarget; // UML: notify: INotify (this would be the User)
    private User user; // Convenience to access User object directly

    public Reservation(Book book, Date reservationDate, String status, User user) {
        this.book = book;
        this.reservationDate = reservationDate;
        this.status = status;
        this.user = user;
        this.notifyTarget = user; // User implements INotify
    }

    // Getters from UML
    public Date getReservationDate() { return reservationDate; }
    public String getStatus() { return status; }
    public Book getBook() { return book; }
    public INotify getNotifyTarget() { return notifyTarget; }
    public User getUser() { return user; }


    // Setters
    public void setStatus(String status) {
        this.status = status;
        // notifyTarget.recieveMessage("Reservation status for '" + book.getTitle() + "' updated to: " + status);
    }

    // Method from UML (Visitor pattern)
    @Override
    public void accept(Executor executor) {
        executor.executeReservation(this);
    }
}