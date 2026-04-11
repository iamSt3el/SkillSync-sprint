package com.skillsync.groupservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class GroupMemberResponseDTO {
    private Long id;
    private Long groupId;
    private Long userId;
    private LocalDate joinedAt;
}
