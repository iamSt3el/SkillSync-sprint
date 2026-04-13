package com.skillsync.groupservice.service;

import com.skillsync.groupservice.dto.mapper.GroupMemberMapper;
import com.skillsync.groupservice.dto.request.GroupMemberRequestDTO;
import com.skillsync.groupservice.dto.response.GroupMemberResponseDTO;
import com.skillsync.groupservice.entity.Group;
import com.skillsync.groupservice.entity.GroupMember;
import com.skillsync.groupservice.event.GroupNotificationProducer;
import com.skillsync.groupservice.exception.GroupMemberNotFoundException;
import com.skillsync.groupservice.exception.GroupNotFoundException;
import com.skillsync.groupservice.exception.MemberAlreadyInGroupException;
import com.skillsync.groupservice.repository.GroupMemberRepository;
import com.skillsync.groupservice.repository.GroupRepository;
import com.skillsync.groupservice.service.impl.GroupMemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupMemberServiceImplTest {

    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private GroupMemberMapper groupMemberMapper;
    @Mock private GroupNotificationProducer groupNotificationProducer;

    @InjectMocks private GroupMemberServiceImpl groupMemberService;

    private Group group;
    private GroupMember member;
    private GroupMemberRequestDTO memberRequest;

    @BeforeEach
    void setUp() {
        group = new Group();
        group.setId(1L);
        group.setName("Spring Boot Learners");
        group.setCreatedBy(5L);
        group.setActive(true);

        member = GroupMember.builder()
                .id(10L)
                .group(group)
                .userId(20L)
                .build();

        memberRequest = GroupMemberRequestDTO.builder().userId(20L).build();
    }

    // ── joinGroup ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("joinGroup: adds user to group and publishes event")
    void joinGroup_success() {
        GroupMemberResponseDTO responseDTO = GroupMemberResponseDTO.builder()
                .id(10L).groupId(1L).userId(20L).build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findFirstByGroupIdAndUserId(1L, 20L)).thenReturn(Optional.empty());
        when(groupMemberMapper.toEntity(memberRequest, group)).thenReturn(member);
        when(groupMemberRepository.save(member)).thenReturn(member);
        when(groupMemberMapper.toResponseDto(member)).thenReturn(responseDTO);

        GroupMemberResponseDTO result = groupMemberService.joinGroup(memberRequest, 1L);

        assertThat(result.getUserId()).isEqualTo(20L);
        verify(groupMemberRepository).save(member);
        verify(groupNotificationProducer).publishMemberJoined(any());
    }

    @Test
    @DisplayName("joinGroup: throws MemberAlreadyInGroupException when user is already a member")
    void joinGroup_alreadyMember() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findFirstByGroupIdAndUserId(1L, 20L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> groupMemberService.joinGroup(memberRequest, 1L))
                .isInstanceOf(MemberAlreadyInGroupException.class)
                .hasMessageContaining("20");

        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("joinGroup: throws GroupNotFoundException when group does not exist")
    void joinGroup_groupNotFound() {
        when(groupRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupMemberService.joinGroup(memberRequest, 99L))
                .isInstanceOf(GroupNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── leaveGroup ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("leaveGroup: removes member from group and publishes event")
    void leaveGroup_success() {
        when(groupMemberRepository.findFirstByGroupIdAndUserId(1L, 20L)).thenReturn(Optional.of(member));

        groupMemberService.leaveGroup(memberRequest, 1L);

        verify(groupMemberRepository).delete(member);
        verify(groupNotificationProducer).publishMemberLeft(any());
    }

    @Test
    @DisplayName("leaveGroup: throws GroupMemberNotFoundException when member not in group")
    void leaveGroup_memberNotFound() {
        when(groupMemberRepository.findFirstByGroupIdAndUserId(1L, 99L)).thenReturn(Optional.empty());

        GroupMemberRequestDTO nonMember = GroupMemberRequestDTO.builder().userId(99L).build();

        assertThatThrownBy(() -> groupMemberService.leaveGroup(nonMember, 1L))
                .isInstanceOf(GroupMemberNotFoundException.class);

        verify(groupMemberRepository, never()).delete(any());
    }
}
