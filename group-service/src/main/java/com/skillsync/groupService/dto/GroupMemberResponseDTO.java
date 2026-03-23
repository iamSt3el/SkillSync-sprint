package com.skillsync.groupService.dto;

import java.time.LocalDate;

import com.skillsync.groupService.entity.Group;
import com.skillsync.groupService.entity.GroupMember;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class GroupMemberResponseDTO {
	private Long id;
	private Long groupId;
	private	Long userId;
	private LocalDate joinedAt;
}
