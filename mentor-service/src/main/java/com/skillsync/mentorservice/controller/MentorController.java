package com.skillsync.mentorservice.controller;

import com.skillsync.mentorservice.dto.request.AvailabilityRequest;
import com.skillsync.mentorservice.dto.request.MentorApplyRequest;
import com.skillsync.mentorservice.dto.response.MentorResponse;
import com.skillsync.mentorservice.service.MentorDiscoveryService;
import com.skillsync.mentorservice.service.MentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;
    private final MentorDiscoveryService mentorDiscoveryService;

    @PostMapping("/apply")
    public ResponseEntity<MentorResponse> applyAsMentor(
            @Valid @RequestBody MentorApplyRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("POST /mentors/apply - email={}", email);
        return ResponseEntity.status(HttpStatus.CREATED).body(mentorService.applyAsMentor(request, email));
    }

    /**
     * GET /mentors?page=0&size=12&sortBy=rating
     * Returns paginated active mentors, optionally filtered/sorted.
     */
    @GetMapping
    public ResponseEntity<Page<MentorResponse>> getMentors(
            @RequestParam(required = false) Long skillId,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRate,
            @RequestParam(required = false) Integer minExp,
            @RequestParam(required = false, defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, toSort(sortBy));

        if (skillId == null && minRating == null && maxRate == null && minExp == null) {
            return ResponseEntity.ok(mentorService.getAllActiveMentorsPaged(pageable));
        }
        return ResponseEntity.ok(mentorDiscoveryService.searchMentors(skillId, minRating, maxRate, minExp, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentorResponse> getMentorById(@PathVariable Long id) {
        log.info("GET /mentors/{}", id);
        return ResponseEntity.ok(mentorService.getMentorById(id));
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<MentorResponse> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("PUT /mentors/{}/availability - userId={}", id, userId);
        return ResponseEntity.ok(mentorService.updateAvailability(id, request, userId));
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> mentorExists(@PathVariable Long id) {
        log.info("GET /mentors/{}/exists", id);
        return ResponseEntity.ok(mentorService.mentorExists(id));
    }

    private Sort toSort(String sortBy) {
        return switch (sortBy == null ? "rating" : sortBy) {
            case "rate_asc"   -> Sort.by(Sort.Direction.ASC,  "hourlyRate");
            case "rate_desc"  -> Sort.by(Sort.Direction.DESC, "hourlyRate");
            case "experience" -> Sort.by(Sort.Direction.DESC, "experience");
            default           -> Sort.by(Sort.Direction.DESC, "rating");
        };
    }
}
