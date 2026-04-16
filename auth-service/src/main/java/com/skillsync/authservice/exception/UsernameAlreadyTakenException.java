package com.skillsync.authservice.exception;

public class UsernameAlreadyTakenException extends RuntimeException {
    public UsernameAlreadyTakenException(String username) {
        super("Username is already taken: " + username);
    }
}
