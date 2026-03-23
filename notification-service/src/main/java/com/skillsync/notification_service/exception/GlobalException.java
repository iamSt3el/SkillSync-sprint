package com.skillsync.notification_service.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.skillsync.notification_service.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalException {
	@ExceptionHandler(NotificationNotFoundException.class)
	public ResponseEntity<?> handleNotificationNotFound(NotificationNotFoundException ex){
		return ResponseEntity.status(404).body(ex.getMessage());
	}
	
	 @ExceptionHandler(EmailSendException.class)
	    public ResponseEntity<ErrorResponse> handleEmailSendException(EmailSendException e) {
	        ErrorResponse error = new ErrorResponse(
	            "EMAIL_SEND_FAILED",
	            e.getMessage(),
	            LocalDateTime.now()
	        );
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	    }
}
