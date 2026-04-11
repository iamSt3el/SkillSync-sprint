package com.skillsync.userservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "profile_picture_url", columnDefinition = "TEXT")
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    public enum Status {
        ACTIVE,
        BLOCKED
    }

    public User(String username, String name, String email, String password, String role) {
        this.username  = username;
        this.name      = name;
        this.email     = email;
        this.password  = password;
        this.role      = role;
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String name, String email, String password, String role, String profilePictureUrl) {
        this(username, name, email, password, role);
        this.profilePictureUrl = profilePictureUrl;
    }
}
