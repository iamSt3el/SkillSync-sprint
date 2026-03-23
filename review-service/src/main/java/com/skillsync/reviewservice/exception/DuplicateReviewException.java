package com.skillsync.reviewservice.exception;

public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException(String message) {
        super(message);
    }
}
