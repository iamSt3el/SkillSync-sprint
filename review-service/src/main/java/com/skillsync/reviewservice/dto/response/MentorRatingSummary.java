package com.skillsync.reviewservice.dto.response;

public record MentorRatingSummary(
        Long mentorId,
        Double averageRating,
        Long totalReviews
) {}
