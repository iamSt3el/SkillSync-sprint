package com.skillsync.groupservice.dto;

import java.time.LocalDate;

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
