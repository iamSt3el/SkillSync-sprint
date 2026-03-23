package com.skillsync.reviewservice.service;

import com.skillsync.reviewservice.dto.request.ReviewSubmitRequest;
import com.skillsync.reviewservice.dto.response.MentorRatingSummary;
import com.skillsync.reviewservice.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse submitReview(ReviewSubmitRequest request);

    List<ReviewResponse> getReviewsByMentorId(Long mentorId);

    MentorRatingSummary getMentorRatingSummary(Long mentorId);
}
