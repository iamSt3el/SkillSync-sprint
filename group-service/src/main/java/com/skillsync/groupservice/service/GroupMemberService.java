package com.skillsync.groupservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsync.groupservice.dto.GroupMapper;
import com.skillsync.groupservice.dto.GroupMemberMapper;
import com.skillsync.groupservice.dto.GroupMemberRequestDTO;
import com.skillsync.groupservice.dto.GroupMemberResponseDTO;
import com.skillsync.groupservice.entity.EventType;
import com.skillsync.groupservice.entity.Group;
import com.skillsync.groupservice.entity.GroupMember;
import com.skillsync.groupservice.event.GroupNotificationProducer;
import com.skillsync.groupservice.event.UserNotificationEvent;
import com.skillsync.groupservice.exception.GroupMemberNotFoundException;
import com.skillsync.groupservice.exception.GroupNotFoundException;
import com.skillsync.groupservice.exception.MemberAlreadyInGroupException;
import com.skillsync.groupservice.repository.GroupMemberRepository;
import com.skillsync.groupservice.repository.GroupRepository;

@Slf4j
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
			log.warn("User already in group: userId={}, groupId={}", dto.getUserId(), groupId);
			throw new MemberAlreadyInGroupException(dto.getUserId());
		}

		GroupMember member = groupMemberMapper.toEntity(dto, group);
		GroupMember saved = groupMemberRepository.save(member);
		log.info("User joined group: userId={}, groupId={}", dto.getUserId(), groupId);

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
		log.info("User left group: userId={}, groupId={}", dto.getUserId(), groupId);

		UserNotificationEvent event = UserNotificationEvent.builder()
				.groupId(group.getId())
				.groupName(group.getName())
				.userId(dto.getUserId())
				.adminId(group.getCreatedBy())
				.eventType(EventType.LEFT)
				.build();
		groupNotificationProducer.publishMemberLeft(event);
	}
}
