package com.skillsync.mentorservice.controller;

import com.skillsync.mentorservice.dto.response.MentorResponse;
import com.skillsync.mentorservice.service.MentorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/mentors")
@RequiredArgsConstructor
public class AdminMentorController {

    private final MentorService mentorService;

    @GetMapping
    public ResponseEntity<List<MentorResponse>> getAllMentors() {
        log.info("GET /admin/mentors");
        return ResponseEntity.ok(mentorService.getAllMentors());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<MentorResponse> approveMentor(@PathVariable Long id) {
        log.info("PUT /admin/mentors/{}/approve", id);
        return ResponseEntity.ok(mentorService.approveMentor(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMentor(@PathVariable Long id) {
        log.info("DELETE /admin/mentors/{}", id);
        mentorService.deleteMentor(id);
        return ResponseEntity.ok("Mentor deleted successfully");
    }
}
