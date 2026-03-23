package com.skillsync.groupService.service;

import org.springframework.stereotype.Service;

import com.skillsync.groupService.dto.GroupMapper;
import com.skillsync.groupService.dto.GroupMemberMapper;
import com.skillsync.groupService.dto.GroupMemberRequestDTO;
import com.skillsync.groupService.dto.GroupMemberResponseDTO;
import com.skillsync.groupService.entity.Group;
import com.skillsync.groupService.entity.GroupMember;
import com.skillsync.groupService.exception.GroupMemberNotFoundException;
import com.skillsync.groupService.exception.GroupNotFoundException;
import com.skillsync.groupService.exception.MemberAlreadyInGroupException;
import com.skillsync.groupService.repository.GroupMemberRepository;
import com.skillsync.groupService.repository.GroupRepository;

@Service
public class GroupMemberService {
	private final GroupMemberRepository groupMemberRepository;
	private final GroupRepository groupRepository;
	private final GroupMemberMapper groupMemberMapper;

	public GroupMemberService(GroupMemberRepository groupMemberRepository, GroupMemberMapper groupMemberMapper,
			GroupRepository groupRepository) {
		this.groupMemberRepository = groupMemberRepository;
		this.groupMemberMapper = groupMemberMapper;
		this.groupRepository = groupRepository;
	}

	public GroupMemberResponseDTO joinGroup(GroupMemberRequestDTO dto, Long groupId) {
		Group group = groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
		boolean alreadyMember = groupMemberRepository.findByGroupIdAndUserId(group.getId(), dto.getUserId())
				.isPresent();

		if (alreadyMember) {
			throw new MemberAlreadyInGroupException(dto.getUserId());
		}

		GroupMember member = groupMemberMapper.toEntity(dto, group);
		GroupMember saved = groupMemberRepository.save(member);
		return groupMemberMapper.toResponseDto(saved);
	}

	public void leaveGroup(GroupMemberRequestDTO dto, Long groupId) {
		GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, dto.getUserId())
				.orElseThrow(() -> new GroupMemberNotFoundException(dto.getUserId()));
		groupMemberRepository.delete(member);
	};
}
