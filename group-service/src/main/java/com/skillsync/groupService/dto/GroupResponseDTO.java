package com.skillsync.groupService.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GroupResponseDTO {
	private Long id;
	private String name;
	private String description;
	private Long createdBy;
	private boolean isActive;
	private LocalDate createdAt;
}
