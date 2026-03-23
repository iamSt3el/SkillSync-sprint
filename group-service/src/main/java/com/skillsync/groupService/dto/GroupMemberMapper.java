package com.skillsync.groupService.dto;

import org.springframework.stereotype.Component;

import com.skillsync.groupService.entity.Group;
import com.skillsync.groupService.entity.GroupMember;

@Component
public class GroupMemberMapper {
	public GroupMemberResponseDTO toResponseDto(GroupMember member) {
	    return GroupMemberResponseDTO.builder()
	            .id(member.getId())
	            .userId(member.getUserId())
	            .groupId(member.getGroup().getId())
	            .joinedAt(member.getJoinedAt())
	            .build();
	}
	
	public GroupMember toEntity(GroupMemberRequestDTO dto, Group group) {
	    return GroupMember.builder()
	            .group(group)
	            .userId(dto.getUserId())
	            .build();
	}
}
