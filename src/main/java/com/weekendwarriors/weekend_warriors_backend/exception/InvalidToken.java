package com.weekendwarriors.weekend_warriors_backend.exception;

public class InvalidToken extends RuntimeException {
    public InvalidToken(String message) {
        super(message);
    }
}
