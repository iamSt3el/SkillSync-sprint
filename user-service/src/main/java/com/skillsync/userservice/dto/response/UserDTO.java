package com.skillsync.userservice.dto.response;

import com.skillsync.userservice.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private String profilePictureUrl;

    public UserDTO(User user) {
        this.id                = user.getId();
        this.username          = user.getUsername();
        this.name              = user.getName();
        this.email             = user.getEmail();
        this.role              = user.getRole();
        this.createdAt         = user.getCreatedAt();
        this.profilePictureUrl = user.getProfilePictureUrl();
    }

    @Override
    public String toString() {
        return "UserDTO{id=" + id + ", username='" + username + "', name='" + name +
               "', email='" + email + "', role='" + role + "', createdAt=" + createdAt + '}';
    }
}
