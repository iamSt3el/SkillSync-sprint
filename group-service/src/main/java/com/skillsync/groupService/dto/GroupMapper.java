package com.skillsync.groupService.dto;

import org.springframework.stereotype.Component;

import com.skillsync.groupService.entity.Group;

@Component
public class GroupMapper {
	public GroupResponseDTO toResponseDto(Group group) {
		return new GroupResponseDTO(
				group.getId(),
				group.getName(),
				group.getDescription(),
				group.getCreatedBy(),
				group.isActive(),
				group.getCreatedAt()
			);
	}
	
	public Group toEntity(GroupRequestDTO dto) {
		Group group = new Group();
		group.setName(dto.getName());
		group.setDescription(dto.getDescription());
		group.setCreatedBy(dto.getCreatedBy());
		
		return group;
	}
}
