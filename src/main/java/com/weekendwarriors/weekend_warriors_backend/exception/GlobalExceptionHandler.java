package com.weekendwarriors.weekend_warriors_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", "Incorrect input");
        List<String> errorMessages = new ArrayList<>();
        for (ObjectError error : bindingResult.getAllErrors()) {
            errorMessages.add(error.getDefaultMessage());
        }
        responseBody.put("details", errorMessages);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    @ExceptionHandler(InvalidToken.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTokenErrors(InvalidToken exception) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }

    @ExceptionHandler(NotAuthenticated.class)
    public ResponseEntity<Map<String, Object>> handleNotAuthenticatedErrors(NotAuthenticated exception) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIOException(IOException exception){
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("error", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException exception){
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("error", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception exception) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("error", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }
}
