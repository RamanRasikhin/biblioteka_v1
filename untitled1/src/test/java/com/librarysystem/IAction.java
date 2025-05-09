package com.librarysystem;

// This interface is mentioned in LibraryCard's commands but not fully defined in the provided UML.
// IPerform seems to serve this role for Borrow and Reservation.
// If other actions exist, they would implement this.
public interface IAction extends com.librarysystem.IPerform {
    // Potentially common methods for all actions, if any.
    // For now, IPerform.accept(Executor) is the key method.
}
