package com.librarysystem;

// Corresponds to UML IPerform
public interface IPerform {
    void accept(Executor executor); // Visitor pattern
}
