package com.skillsync.groupservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillsync.groupservice.dto.GroupResponseDTO;
import com.skillsync.groupservice.service.GroupService;

import lombok.RequiredArgsConstructor;

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
	public ResponseEntity<String> deleteGroup(@PathVariable Long id) {
		groupService.deleteGroup(id);
		return ResponseEntity.ok("Group deleted successfully");
	}

	@PutMapping("/{id}/deactivate")
	public ResponseEntity<String> deactivateGroup(@PathVariable Long id) {
		groupService.deactivateGroup(id);
		return ResponseEntity.ok("Group deactivated successfully");
	}
}
