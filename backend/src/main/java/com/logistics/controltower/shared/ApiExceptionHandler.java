package com.logistics.controltower.shared;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException exception) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(), "message", error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<?> constraint(ConstraintViolationException exception) {
        return response(HttpStatus.BAD_REQUEST, "Validation failed", exception.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<?> status(ResponseStatusException exception) {
        return response(HttpStatus.valueOf(exception.getStatusCode().value()),
                exception.getReason() == null ? "Request failed" : exception.getReason(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<?> denied() {
        return response(HttpStatus.FORBIDDEN, "Access denied", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflict() {
        return response(HttpStatus.CONFLICT, "The request conflicts with existing data", null);
    }

    private ResponseEntity<?> response(HttpStatus status, String message, Object details) {
        var body = details == null
                ? Map.of("timestamp", Instant.now(), "status", status.value(), "error", message)
                : Map.of("timestamp", Instant.now(), "status", status.value(), "error", message, "details", details);
        return ResponseEntity.status(status).body(body);
    }
}
