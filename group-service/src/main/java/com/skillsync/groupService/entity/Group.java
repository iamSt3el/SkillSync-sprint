package com.skillsync.groupService.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skills_groups")
@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class Group {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, unique = true, length = 150)
	private String name;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	@Column(name = "created_by", nullable = false)
	private Long createdBy;
	
	@Column(name = "is_active")
	private boolean isActive = true;
	
	@OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GroupMember> members = new ArrayList<>();
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDate createdAt;
}
