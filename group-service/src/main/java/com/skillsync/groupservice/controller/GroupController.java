package com.skillsync.groupservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillsync.groupservice.dto.GroupMemberRequestDTO;
import com.skillsync.groupservice.dto.GroupMemberResponseDTO;
import com.skillsync.groupservice.dto.GroupRequestDTO;
import com.skillsync.groupservice.dto.GroupResponseDTO;
import com.skillsync.groupservice.service.GroupMemberService;
import com.skillsync.groupservice.service.GroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
	private final GroupService groupService;
	private final GroupMemberService groupMemberService;
	
	@PostMapping
	public ResponseEntity<GroupResponseDTO> createGroup(@RequestBody @Valid GroupRequestDTO dto){
		return ResponseEntity.ok(groupService.createGroup(dto));
	}
	
	@GetMapping
	public ResponseEntity<List<GroupResponseDTO>> getAllGroups(){
		return ResponseEntity.ok(groupService.getAllGroups());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<GroupResponseDTO> getGroupById(@PathVariable Long id){
		return ResponseEntity.ok(groupService.getGroupById(id));
	}
	
	@PostMapping("/{id}/join")
	public ResponseEntity<GroupMemberResponseDTO> joinGroup( @PathVariable Long id, @RequestBody @Valid GroupMemberRequestDTO dto){
		return ResponseEntity.ok(groupMemberService.joinGroup(dto, id));
	}
	
	@PostMapping("/{id}/leave")
	public ResponseEntity<String> leaveGroup(@PathVariable Long id, @RequestBody @Valid GroupMemberRequestDTO dto){
		groupMemberService.leaveGroup(dto, id);
		return ResponseEntity.ok("Successfully left the group");
	}
}
