package com.skillsync.groupService.exception;

import com.skillsync.groupService.dto.GroupMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(GroupNotFoundException.class)
	public ResponseEntity<?> handleGroupNotFound(GroupNotFoundException ex){
		return ResponseEntity.status(404).body(ex.getMessage());
	}
	
	@ExceptionHandler(GroupMemberNotFoundException.class)
	public ResponseEntity<?> handleGroupMemberNotFound(GroupMemberNotFoundException ex){
		return ResponseEntity.status(409).body(ex.getMessage());
	}
	
	@ExceptionHandler(MemberAlreadyInGroupException.class)
	public ResponseEntity<?> handleMemberAlreadyInGroup(MemberAlreadyInGroupException ex){
		return ResponseEntity.status(404).body(ex.getMessage());
	}
}
