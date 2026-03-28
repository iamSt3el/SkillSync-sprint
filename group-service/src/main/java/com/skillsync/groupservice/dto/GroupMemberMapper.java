package com.skillsync.groupservice.dto;

import org.springframework.stereotype.Component;

import com.skillsync.groupservice.entity.Group;
import com.skillsync.groupservice.entity.GroupMember;

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
