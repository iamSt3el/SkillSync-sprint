package com.skillsync.groupservice.service;

import com.skillsync.groupservice.dto.mapper.GroupMapper;
import com.skillsync.groupservice.dto.request.GroupRequestDTO;
import com.skillsync.groupservice.dto.response.GroupResponseDTO;
import com.skillsync.groupservice.entity.Group;
import com.skillsync.groupservice.exception.GroupNotFoundException;
import com.skillsync.groupservice.repository.GroupRepository;
import com.skillsync.groupservice.service.impl.GroupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock private GroupRepository groupRepository;
    @Mock private GroupMapper groupMapper;

    @InjectMocks private GroupServiceImpl groupService;

    private Group group;
    private GroupResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        group = new Group();
        group.setId(1L);
        group.setName("Spring Boot Learners");
        group.setDescription("A group for Spring Boot enthusiasts");
        group.setCreatedBy(5L);
        group.setActive(true);

        responseDTO = new GroupResponseDTO(1L, "Spring Boot Learners",
                "A group for Spring Boot enthusiasts", 5L, true, null);
    }

    // ── createGroup ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createGroup: saves group and returns DTO")
    void createGroup_success() {
        GroupRequestDTO request = GroupRequestDTO.builder()
                .name("Spring Boot Learners")
                .description("A group for Spring Boot enthusiasts")
                .createdBy(5L)
                .build();

        when(groupMapper.toEntity(request)).thenReturn(group);
        when(groupRepository.save(group)).thenReturn(group);
        when(groupMapper.toResponseDto(group)).thenReturn(responseDTO);

        GroupResponseDTO result = groupService.createGroup(request);

        assertThat(result.getName()).isEqualTo("Spring Boot Learners");
        verify(groupRepository).save(group);
    }

    // ── getAllGroups ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllGroups: returns all groups as DTOs")
    void getAllGroups_returnsList() {
        when(groupRepository.findAll()).thenReturn(List.of(group));
        when(groupMapper.toResponseDto(group)).thenReturn(responseDTO);

        List<GroupResponseDTO> result = groupService.getAllGroups();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAllGroups: returns empty list when no groups exist")
    void getAllGroups_empty() {
        when(groupRepository.findAll()).thenReturn(List.of());

        List<GroupResponseDTO> result = groupService.getAllGroups();

        assertThat(result).isEmpty();
    }

    // ── getGroupById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getGroupById: returns DTO when group found")
    void getGroupById_found() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupMapper.toResponseDto(group)).thenReturn(responseDTO);

        GroupResponseDTO result = groupService.getGroupById(1L);

        assertThat(result.getName()).isEqualTo("Spring Boot Learners");
    }

    @Test
    @DisplayName("getGroupById: throws GroupNotFoundException when not found")
    void getGroupById_notFound() {
        when(groupRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.getGroupById(99L))
                .isInstanceOf(GroupNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── deleteGroup ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteGroup: deletes group when found")
    void deleteGroup_success() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        groupService.deleteGroup(1L);

        verify(groupRepository).delete(group);
    }

    @Test
    @DisplayName("deleteGroup: throws GroupNotFoundException when not found")
    void deleteGroup_notFound() {
        when(groupRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.deleteGroup(99L))
                .isInstanceOf(GroupNotFoundException.class);

        verify(groupRepository, never()).delete(any());
    }

    // ── deactivateGroup ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deactivateGroup: sets isActive to false")
    void deactivateGroup_success() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        groupService.deactivateGroup(1L);

        assertThat(group.isActive()).isFalse();
        verify(groupRepository).save(group);
    }

    @Test
    @DisplayName("deactivateGroup: throws GroupNotFoundException when not found")
    void deactivateGroup_notFound() {
        when(groupRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.deactivateGroup(99L))
                .isInstanceOf(GroupNotFoundException.class);
    }

    // ── getGroupsByUserId ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getGroupsByUserId: returns groups the user is a member of")
    void getGroupsByUserId_returnsList() {
        when(groupRepository.findByMembersUserId(5L)).thenReturn(List.of(group));
        when(groupMapper.toResponseDto(group)).thenReturn(responseDTO);

        List<GroupResponseDTO> result = groupService.getGroupsByUserId(5L);

        assertThat(result).hasSize(1);
    }
}
