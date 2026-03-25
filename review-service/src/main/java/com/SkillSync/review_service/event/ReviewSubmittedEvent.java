package com.SkillSync.review_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewSubmittedEvent {
    private Long reviewId;
    private Long mentorId;
    private Long userId;
    private Double rating;
}
