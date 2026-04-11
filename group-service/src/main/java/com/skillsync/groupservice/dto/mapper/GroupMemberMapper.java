package com.skillsync.groupservice.dto.mapper;

import com.skillsync.groupservice.dto.request.GroupMemberRequestDTO;
import com.skillsync.groupservice.dto.response.GroupMemberResponseDTO;
import com.skillsync.groupservice.entity.Group;
import com.skillsync.groupservice.entity.GroupMember;
import org.springframework.stereotype.Component;

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
