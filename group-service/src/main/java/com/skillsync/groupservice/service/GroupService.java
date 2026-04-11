package com.skillsync.groupservice.service;

import com.skillsync.groupservice.dto.request.GroupRequestDTO;
import com.skillsync.groupservice.dto.response.GroupResponseDTO;

import java.util.List;

public interface GroupService {

    GroupResponseDTO createGroup(GroupRequestDTO dto);

    List<GroupResponseDTO> getAllGroups();

    GroupResponseDTO getGroupById(Long id);

    void deleteGroup(Long id);

    void deactivateGroup(Long id);

    List<GroupResponseDTO> getGroupsByUserId(Long userId);
}
