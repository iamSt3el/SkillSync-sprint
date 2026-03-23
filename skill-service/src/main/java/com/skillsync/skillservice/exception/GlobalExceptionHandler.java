package com.skillsync.skillservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SkillNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(SkillNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiError.builder()
                .status(404)
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiError.builder()
                .status(409)
                .error("Conflict")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError.builder()
                .status(400)
                .error("Validation Failed")
                .message(errors)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiError.builder()
                .status(500)
                .error("Internal Server Error")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}
