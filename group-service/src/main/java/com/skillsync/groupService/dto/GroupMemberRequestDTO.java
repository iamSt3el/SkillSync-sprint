package com.skillsync.groupService.dto;

import java.time.LocalDate;

import com.skillsync.groupService.entity.Group;
import com.skillsync.groupService.entity.GroupMember;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class GroupMemberRequestDTO {
	
	@NotNull(message = "User id id required")
	private Long userId;
}
