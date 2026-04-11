package com.skillsync.mentorservice.controller;

import com.skillsync.mentorservice.dto.response.MentorResponse;
import com.skillsync.mentorservice.dto.response.MentorStatsDTO;
import com.skillsync.mentorservice.service.MentorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/mentors")
@RequiredArgsConstructor
public class AdminMentorController {

    private final MentorService mentorService;

    @GetMapping
    public ResponseEntity<Page<MentorResponse>> getAllMentors(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "15") int size) {
        log.info("GET /admin/mentors?page={}&size={}", page, size);
        return ResponseEntity.ok(mentorService.getAllMentorsPaged(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/stats")
    public ResponseEntity<MentorStatsDTO> getMentorStats() {
        log.info("GET /admin/mentors/stats");
        return ResponseEntity.ok(mentorService.getMentorStats());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<MentorResponse> approveMentor(@PathVariable Long id) {
        log.info("PUT /admin/mentors/{}/approve", id);
        return ResponseEntity.ok(mentorService.approveMentor(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<MentorResponse> rejectMentor(@PathVariable Long id) {
        log.info("PUT /admin/mentors/{}/reject", id);
        return ResponseEntity.ok(mentorService.rejectMentor(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMentor(@PathVariable Long id) {
        log.info("DELETE /admin/mentors/{}", id);
        mentorService.deleteMentor(id);
        return ResponseEntity.ok("Mentor deleted successfully");
    }
}
