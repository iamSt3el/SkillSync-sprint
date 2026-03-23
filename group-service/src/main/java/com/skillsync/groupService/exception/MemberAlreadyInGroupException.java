package com.skillsync.groupService.exception;

public class MemberAlreadyInGroupException extends RuntimeException{
	public MemberAlreadyInGroupException(Long id) {
		super("Member is already in the group by id: " + id);
	}
}
