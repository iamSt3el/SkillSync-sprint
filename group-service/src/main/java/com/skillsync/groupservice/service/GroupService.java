package com.skillsync.groupservice.service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.skillsync.groupservice.dto.GroupMapper;
import com.skillsync.groupservice.dto.GroupRequestDTO;
import com.skillsync.groupservice.dto.GroupResponseDTO;
import com.skillsync.groupservice.entity.Group;
import com.skillsync.groupservice.exception.GroupNotFoundException;
import com.skillsync.groupservice.repository.GroupRepository;

@Slf4j
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
		log.info("Group created: id={}", saved.getId());
		return groupMapper.toResponseDto(saved);
	}

	public List<GroupResponseDTO> getAllGroups() {
		return groupRepository.findAll().stream().map(groupMapper::toResponseDto).toList();
	}

	public GroupResponseDTO getGroupById(Long id) {
		Group group = groupRepository.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
		return groupMapper.toResponseDto(group);
	}

	public void deleteGroup(Long id) {
		Group group = groupRepository.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
		groupRepository.delete(group);
		log.info("Group deleted: id={}", id);
	}

	public void deactivateGroup(Long id) {
		Group group = groupRepository.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
		group.setActive(false);
		groupRepository.save(group);
		log.info("Group deactivated: id={}", id);
	}
}
