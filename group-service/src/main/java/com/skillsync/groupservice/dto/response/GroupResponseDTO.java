package com.skillsync.groupservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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
