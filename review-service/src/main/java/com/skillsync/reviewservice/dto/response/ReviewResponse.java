package com.skillsync.reviewservice.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long mentorId,
        Long userId,
        Long sessionId,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}
