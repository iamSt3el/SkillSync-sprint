package com.skillsync.groupservice.controller;

import com.skillsync.groupservice.dto.request.GroupMemberRequestDTO;
import com.skillsync.groupservice.dto.request.GroupRequestDTO;
import com.skillsync.groupservice.dto.response.GroupMemberResponseDTO;
import com.skillsync.groupservice.dto.response.GroupResponseDTO;
import com.skillsync.groupservice.service.GroupMemberService;
import com.skillsync.groupservice.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final GroupMemberService groupMemberService;

    @PostMapping
    public ResponseEntity<GroupResponseDTO> createGroup(@RequestBody @Valid GroupRequestDTO dto) {
        return ResponseEntity.ok(groupService.createGroup(dto));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponseDTO>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponseDTO> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<GroupMemberResponseDTO> joinGroup(@PathVariable Long id,
                                                            @RequestBody @Valid GroupMemberRequestDTO dto) {
        return ResponseEntity.ok(groupMemberService.joinGroup(dto, id));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long id,
                                           @RequestBody @Valid GroupMemberRequestDTO dto) {
        groupMemberService.leaveGroup(dto, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/joined")
    public ResponseEntity<List<GroupResponseDTO>> getJoinedGroups(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(groupService.getGroupsByUserId(userId));
    }
}
