package com.skillsync.sessionservice.controller;

import com.skillsync.sessionservice.dto.response.SessionResponse;
import com.skillsync.sessionservice.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/sessions")
@RequiredArgsConstructor
public class AdminSessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getAllSessions() {
        log.info("GET /admin/sessions");
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<SessionResponse> forceCancel(@PathVariable Long id) {
        log.info("PUT /admin/sessions/{}/cancel", id);
        return ResponseEntity.ok(sessionService.adminCancelSession(id));
    }
}
