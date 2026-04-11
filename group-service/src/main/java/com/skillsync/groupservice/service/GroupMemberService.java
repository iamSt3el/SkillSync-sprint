package com.skillsync.groupservice.service;

import com.skillsync.groupservice.dto.request.GroupMemberRequestDTO;
import com.skillsync.groupservice.dto.response.GroupMemberResponseDTO;

public interface GroupMemberService {

    GroupMemberResponseDTO joinGroup(GroupMemberRequestDTO dto, Long groupId);

    void leaveGroup(GroupMemberRequestDTO dto, Long groupId);
}
