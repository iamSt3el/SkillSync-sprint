package com.skillsync.mentorservice.controller;

import com.skillsync.mentorservice.dto.request.AvailabilityRequest;
import com.skillsync.mentorservice.dto.request.MentorApplyRequest;
import com.skillsync.mentorservice.dto.response.MentorResponse;
import com.skillsync.mentorservice.service.MentorDiscoveryService;
import com.skillsync.mentorservice.service.MentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;
    private final MentorDiscoveryService mentorDiscoveryService;

    /**
     * POST /mentors/apply
     * A learner applies to become a mentor.
     */
    @PostMapping("/apply")
    public ResponseEntity<MentorResponse> applyAsMentor(
            @Valid @RequestBody MentorApplyRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("POST /mentors/apply - email={}", email);
        return ResponseEntity.status(HttpStatus.CREATED).body(mentorService.applyAsMentor(request, email));
    }

    /**
     * GET /mentors
     * Returns all active mentors, optionally filtered/sorted.
     */
    @GetMapping
    public ResponseEntity<List<MentorResponse>> getMentors(
            @RequestParam(required = false) Long skillId,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRate,
            @RequestParam(required = false) Integer minExp,
            @RequestParam(required = false, defaultValue = "rating") String sortBy) {

        if (skillId == null && minRating == null && maxRate == null && minExp == null) {
            return ResponseEntity.ok(mentorService.getAllActiveMentors());
        }
        return ResponseEntity.ok(
                mentorDiscoveryService.searchMentors(skillId, minRating, maxRate, minExp, sortBy));
    }

    /**
     * GET /mentors/{id}
     * Returns a mentor's full profile.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MentorResponse> getMentorById(@PathVariable Long id) {
        log.info("GET /mentors/{}", id);
        return ResponseEntity.ok(mentorService.getMentorById(id));
    }

    /**
     * PUT /mentors/{id}/availability
     * Mentor updates their availability schedule.
     */
    @PutMapping("/{id}/availability")
    public ResponseEntity<MentorResponse> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("PUT /mentors/{}/availability - userId={}", id, userId);
        return ResponseEntity.ok(mentorService.updateAvailability(id, request, userId));
    }

    /**
     * GET /mentors/{id}/exists
     * Internal endpoint used by session-service to validate a mentor before booking.
     * Returns true only if the mentor exists AND is ACTIVE.
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> mentorExists(@PathVariable Long id) {
        log.info("GET /mentors/{}/exists", id);
        return ResponseEntity.ok(mentorService.mentorExists(id));
    }
}
