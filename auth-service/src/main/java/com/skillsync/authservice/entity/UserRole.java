package com.skillsync.authservice.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_roles")
public class UserRole {
	@EmbeddedId
	private UserRoleId id;

	@ManyToOne
	@MapsId("userId") // maps to userId in composite key
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@MapsId("roleId") // maps to roleId in composite key
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	public UserRole() {
	}

	public UserRole(User user, Role role) {
		this.user = user;
		this.role = role;
		this.id = new UserRoleId(user.getId(), role.getId());
	}

	
	public UserRoleId getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Role getRole() {
		return role;
	}

}
