package com.skillsync.reviewservice.exception;

public class SessionNotCompletedException extends RuntimeException {
    public SessionNotCompletedException(String message) {
        super(message);
    }
}
