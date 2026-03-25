package com.SkillSync.review_service.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.SkillSync.review_service.dto.ErrorResponseDTO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHanlder {
	
	@ExceptionHandler(ReviewNotFoundException.class)
	public ResponseEntity<?> handleGroupNotFound(ReviewNotFoundException ex, HttpServletRequest request){
		log.error(ex.getMessage());
		return buildResponse(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", ex.getMessage(), request);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException e,
			HttpServletRequest request) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + ": " + f.getDefaultMessage()).collect(Collectors.joining(", "));
		log.error("Validation failed: {}", message);
		return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDTO> handleGeneral(Exception e, HttpServletRequest request) {
		log.error("Unexpected error: {}", e.getMessage());
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", e.getMessage(), request);
	}
	
	private ResponseEntity<ErrorResponseDTO> buildResponse(HttpStatus status, String error, String message,
			HttpServletRequest request) {
		ErrorResponseDTO response = new ErrorResponseDTO(status.value(), error, message,
				request.getRequestURL().toString(), LocalDateTime.now());
		return ResponseEntity.status(status).body(response);
	}
}
