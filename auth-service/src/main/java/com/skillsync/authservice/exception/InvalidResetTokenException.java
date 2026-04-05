package com.skillsync.authservice.exception;

public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException() {
        super("Password reset link is invalid or has expired");
    }
}
