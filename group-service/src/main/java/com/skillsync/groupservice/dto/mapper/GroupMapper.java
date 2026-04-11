package com.skillsync.groupservice.dto.mapper;

import com.skillsync.groupservice.dto.request.GroupRequestDTO;
import com.skillsync.groupservice.dto.response.GroupResponseDTO;
import com.skillsync.groupservice.entity.Group;
import org.springframework.stereotype.Component;

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
