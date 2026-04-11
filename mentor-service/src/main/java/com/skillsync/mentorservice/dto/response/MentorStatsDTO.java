package com.skillsync.mentorservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorStatsDTO {
    private long total;
    private long active;
    private long pending;
    private long rejected;
    private double avgRating;
    private double avgHourlyRate;
    private long totalReviews;
}
