package com.skillsync.groupservice.service.impl;

import com.skillsync.groupservice.dto.mapper.GroupMapper;
import com.skillsync.groupservice.dto.request.GroupRequestDTO;
import com.skillsync.groupservice.dto.response.GroupResponseDTO;
import com.skillsync.groupservice.entity.Group;
import com.skillsync.groupservice.exception.GroupNotFoundException;
import com.skillsync.groupservice.repository.GroupRepository;
import com.skillsync.groupservice.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    public GroupServiceImpl(GroupRepository groupRepository, GroupMapper groupMapper) {
        this.groupRepository = groupRepository;
        this.groupMapper = groupMapper;
    }

    @Override
    public GroupResponseDTO createGroup(GroupRequestDTO dto) {
        Group group = groupMapper.toEntity(dto);
        Group saved = groupRepository.save(group);
        log.info("Group created: id={}", saved.getId());
        return groupMapper.toResponseDto(saved);
    }

    @Override
    public List<GroupResponseDTO> getAllGroups() {
        return groupRepository.findAll().stream().map(groupMapper::toResponseDto).toList();
    }

    @Override
    public GroupResponseDTO getGroupById(Long id) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
        return groupMapper.toResponseDto(group);
    }

    @Override
    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
        groupRepository.delete(group);
        log.info("Group deleted: id={}", id);
    }

    @Override
    public void deactivateGroup(Long id) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
        group.setActive(false);
        groupRepository.save(group);
        log.info("Group deactivated: id={}", id);
    }

    @Override
    public List<GroupResponseDTO> getGroupsByUserId(Long userId) {
        return groupRepository.findByMembersUserId(userId).stream()
                .map(groupMapper::toResponseDto).toList();
    }
}
