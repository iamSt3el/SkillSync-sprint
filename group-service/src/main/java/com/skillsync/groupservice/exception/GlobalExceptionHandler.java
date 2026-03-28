package com.skillsync.groupservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(GroupNotFoundException.class)
	public ResponseEntity<String> handleGroupNotFound(GroupNotFoundException ex){
		return ResponseEntity.status(404).body(ex.getMessage());
	}

	@ExceptionHandler(GroupMemberNotFoundException.class)
	public ResponseEntity<String> handleGroupMemberNotFound(GroupMemberNotFoundException ex){
		return ResponseEntity.status(409).body(ex.getMessage());
	}

	@ExceptionHandler(MemberAlreadyInGroupException.class)
	public ResponseEntity<String> handleMemberAlreadyInGroup(MemberAlreadyInGroupException ex){
		return ResponseEntity.status(404).body(ex.getMessage());
	}
}
