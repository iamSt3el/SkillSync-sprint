package com.skillsync.groupService.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.skillsync.groupService.dto.GroupMapper;
import com.skillsync.groupService.dto.GroupRequestDTO;
import com.skillsync.groupService.dto.GroupResponseDTO;
import com.skillsync.groupService.entity.Group;
import com.skillsync.groupService.exception.GroupNotFoundException;
import com.skillsync.groupService.repository.GroupRepository;

@Service
public class GroupService {
	private final GroupRepository groupRepository;
	private final GroupMapper groupMapper;

	public GroupService(GroupRepository groupRepository, GroupMapper groupMapper) {
		this.groupRepository = groupRepository;
		this.groupMapper = groupMapper;
	}

	public GroupResponseDTO createGroup(GroupRequestDTO dto) {
		Group group = groupMapper.toEntity(dto);
		Group saved = groupRepository.save(group);
		return groupMapper.toResponseDto(saved);
	}

	public List<GroupResponseDTO> getAllGroups() {
		return groupRepository.findAll().stream().map(groupMapper::toResponseDto).collect(Collectors.toList());
	}

	public GroupResponseDTO getGroupById(Long id) {
		Group group = groupRepository.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
		return groupMapper.toResponseDto(group);
	}

	public void deleteGroup(Long id) {
		Group group = groupRepository.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
		groupRepository.delete(group);
	}

	public void deactivateGroup(Long id) {
		Group group = groupRepository.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
		group.setActive(false);
		groupRepository.save(group);
	}
}
