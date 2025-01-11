package com.weekendwarriors.weekend_warriors_backend.exception;

public class ProductNotFound extends RuntimeException {
    public ProductNotFound(String message) {
        super(message);
    }
}
