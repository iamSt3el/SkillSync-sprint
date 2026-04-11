package com.skillsync.reviewservice.controller;

import com.skillsync.reviewservice.dto.request.ReviewRequestDTO;
import com.skillsync.reviewservice.dto.response.ReviewResponseDTO;
import com.skillsync.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> saveReview(@Valid @RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.ok(reviewService.saveReview(dto));
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewByMentorId(@PathVariable Long mentorId) {
        return ResponseEntity.ok(reviewService.getReviewByMentor(mentorId));
    }
}
