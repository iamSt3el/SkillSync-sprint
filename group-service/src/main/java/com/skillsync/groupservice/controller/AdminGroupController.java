package com.skillsync.groupservice.controller;

import com.skillsync.groupservice.dto.response.GroupResponseDTO;
import com.skillsync.groupservice.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/groups")
@RequiredArgsConstructor
public class AdminGroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<List<GroupResponseDTO>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateGroup(@PathVariable Long id) {
        groupService.deactivateGroup(id);
        return ResponseEntity.noContent().build();
    }
}
