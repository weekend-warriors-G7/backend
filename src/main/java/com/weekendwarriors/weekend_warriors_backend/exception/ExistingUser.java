package com.weekendwarriors.weekend_warriors_backend.exception;

public class ExistingUser extends Exception {
    public ExistingUser(String message) {
        super(message);
    }
}
