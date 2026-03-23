package com.skillsync.mentorservice.exception;

public class MentorNotFoundException extends RuntimeException {
    public MentorNotFoundException(String message) {
        super(message);
    }
}
