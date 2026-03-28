package com.skillsync.groupservice.dto;

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
public class GroupRequestDTO {
	@NotBlank(message = "Group name is required")
	private String name;
	
	@NotBlank(message = "Group description is required")
	private String description;
	
	@NotNull(message = "Creator User id is required")
	private Long createdBy;
}
