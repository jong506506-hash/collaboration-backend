package com.juteak.collaboration.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * User is a person who can own work, answer questions, and receive notifications.
 */
@Entity
@Table(name = "collab_user")
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "employee_number", nullable = false, length = 64, unique = true)
	private String employeeNumber;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "role_title", length = 128)
	private String roleTitle;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_role", nullable = false, length = 32)
	private UserRole userRole;

	@Column(name = "password_hash", length = 255)
	private String passwordHash;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "primary_team_id")
	private TeamEntity primaryTeam;

	@Column(name = "active", nullable = false)
	private boolean active;

	protected UserEntity() {
	}

	public UserEntity(
		String employeeNumber,
		String name,
		String email,
		String roleTitle,
		UserRole userRole,
		String passwordHash,
		TeamEntity primaryTeam,
		boolean active
	) {
		this.employeeNumber = employeeNumber;
		this.name = name;
		this.email = email;
		this.roleTitle = roleTitle;
		this.userRole = userRole;
		this.passwordHash = passwordHash;
		this.primaryTeam = primaryTeam;
		this.active = active;
	}

	public void update(String name, String email, String roleTitle, UserRole userRole, TeamEntity primaryTeam, boolean active) {
		this.name = name;
		this.email = email;
		this.roleTitle = roleTitle;
		this.userRole = userRole;
		this.primaryTeam = primaryTeam;
		this.active = active;
	}

	public void updatePasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public Long getId() {
		return id;
	}

	public String getEmployeeNumber() {
		return employeeNumber;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getRoleTitle() {
		return roleTitle;
	}

	public UserRole getUserRole() {
		return userRole;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public TeamEntity getPrimaryTeam() {
		return primaryTeam;
	}

	public boolean isActive() {
		return active;
	}
}
