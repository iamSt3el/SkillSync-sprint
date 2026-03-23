package com.skillsync.groupService.exception;

public class GroupMemberNotFoundException extends RuntimeException{
	public GroupMemberNotFoundException(Long id) {
		super("Group Member not found with id: " + id);
	}
}
