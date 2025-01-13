package com.weekendwarriors.weekend_warriors_backend.exception;

public class NotAuthenticated extends RuntimeException {
    public NotAuthenticated(String message) {
        super(message);
    }
}
