package com.skillsync.sessionservice.entity;

public enum SessionStatus {
    PENDING_PAYMENT,   // Session created, payment not yet confirmed
    REQUESTED,         // Payment confirmed, awaiting mentor acceptance
    ACCEPTED,          // Mentor accepted — session is booked
    REJECTED,
    COMPLETED,
    CANCELLED
}
