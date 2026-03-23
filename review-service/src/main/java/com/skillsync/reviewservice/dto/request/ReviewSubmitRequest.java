package com.skillsync.reviewservice.dto.request;

import jakarta.validation.constraints.*;

public record ReviewSubmitRequest(
        @NotNull(message = "Mentor ID is required")
        Long mentorId,

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Session ID is required")
        Long sessionId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,

        @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        String comment
) {}
