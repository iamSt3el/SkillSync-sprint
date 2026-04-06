package com.skillsync.groupservice.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.skillsync.groupservice.dto.ErrorResponseDTO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleGroupNotFound(GroupNotFoundException ex, HttpServletRequest request) {
        log.error("Group not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "GROUP_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(GroupMemberNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleGroupMemberNotFound(GroupMemberNotFoundException ex, HttpServletRequest request) {
        log.error("Group member not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "GROUP_MEMBER_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MemberAlreadyInGroupException.class)
    public ResponseEntity<ErrorResponseDTO> handleMemberAlreadyInGroup(MemberAlreadyInGroupException ex, HttpServletRequest request) {
        log.error("Member already in group: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "MEMBER_ALREADY_IN_GROUP", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("Validation failed: {}", message);
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred.", request);
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String error, String message, HttpServletRequest request) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                status.value(), error, message,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(body);
    }
}
