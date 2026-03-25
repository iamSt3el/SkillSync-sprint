package com.SkillSync.review_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SkillSync.review_service.dto.ReviewRequestDTO;
import com.SkillSync.review_service.dto.ReviewResponseDTO;
import com.SkillSync.review_service.service.ReviewService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
	private final ReviewService reviewService;
	
	@PostMapping()
	public ResponseEntity<ReviewResponseDTO> saveReview(@Valid @RequestBody ReviewRequestDTO dto){
		ReviewResponseDTO response = reviewService.saveReview(dto);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/mentor/{id}")
	public ResponseEntity<List<ReviewResponseDTO>> getReviewByMentorId(@Valid @PathVariable Long mentor_id){
		List<ReviewResponseDTO> reviews = reviewService.getReviewByMentor(mentor_id);
		return ResponseEntity.ok(reviews);
	}
}
