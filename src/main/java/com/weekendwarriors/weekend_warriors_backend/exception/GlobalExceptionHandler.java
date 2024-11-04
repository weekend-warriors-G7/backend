package com.weekendwarriors.weekend_warriors_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExistingUser.class)
    public ResponseEntity<Map<String, String>> handleExistingUserException(ExistingUser exception)
    {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("error", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseBody);
    }

    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFound exception)
    {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("error", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception exception) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("error", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }
}
