package com.skillsync.groupService.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsync.groupService.dto.GroupMapper;
import com.skillsync.groupService.dto.GroupMemberMapper;
import com.skillsync.groupService.dto.GroupMemberRequestDTO;
import com.skillsync.groupService.dto.GroupMemberResponseDTO;
import com.skillsync.groupService.entity.EventType;
import com.skillsync.groupService.entity.Group;
import com.skillsync.groupService.entity.GroupMember;
import com.skillsync.groupService.event.GroupNotificationProducer;
import com.skillsync.groupService.event.UserNotificationEvent;
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
	private final GroupNotificationProducer groupNotificationProducer;

	public GroupMemberService(GroupMemberRepository groupMemberRepository, GroupMemberMapper groupMemberMapper,
			GroupRepository groupRepository, GroupNotificationProducer groupNotificationProducer) {
		this.groupMemberRepository = groupMemberRepository;
		this.groupMemberMapper = groupMemberMapper;
		this.groupRepository = groupRepository;
		this.groupNotificationProducer = groupNotificationProducer;
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

		UserNotificationEvent event = UserNotificationEvent.builder()
				.groupId(group.getId())
				.groupName(group.getName())
				.userId(dto.getUserId())
				.adminId(group.getCreatedBy())
				.eventType(EventType.JOINED)
				.build();
		groupNotificationProducer.publishMemberJoined(event);

		return groupMemberMapper.toResponseDto(saved);
	}

	@Transactional
	public void leaveGroup(GroupMemberRequestDTO dto, Long groupId) {
		GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, dto.getUserId())
				.orElseThrow(() -> new GroupMemberNotFoundException(dto.getUserId()));

		Group group = member.getGroup();
		groupMemberRepository.delete(member);

		UserNotificationEvent event = UserNotificationEvent.builder()
				.groupId(group.getId())
				.groupName(group.getName())
				.userId(dto.getUserId())
				.adminId(group.getCreatedBy())
				.eventType(EventType.LEFT)
				.build();
		groupNotificationProducer.publishMemberLeft(event);
	};
}
