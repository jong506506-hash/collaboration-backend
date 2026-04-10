package com.juteak.collaboration.persistence.entity;

import java.time.LocalDateTime;

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
 * Signup requests allow internal users to ask for an account before an admin approves it.
 */
@Entity
@Table(name = "collab_signup_request")
public class SignupRequestEntity {

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

	@Column(name = "requested_team_code", length = 64)
	private String requestedTeamCode;

	@Column(name = "requested_team_name", length = 128)
	private String requestedTeamName;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_role", nullable = false, length = 32)
	private UserRole userRole;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "request_status", nullable = false, length = 32)
	private SignupRequestStatus requestStatus;

	@Column(name = "review_note", length = 500)
	private String reviewNote;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "reviewed_at")
	private LocalDateTime reviewedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewed_by_user_id")
	private UserEntity reviewedByUser;

	protected SignupRequestEntity() {
	}

	public SignupRequestEntity(
		String employeeNumber,
		String name,
		String email,
		String roleTitle,
		String requestedTeamCode,
		String requestedTeamName,
		UserRole userRole,
		String passwordHash,
		SignupRequestStatus requestStatus,
		String reviewNote,
		LocalDateTime createdAt
	) {
		this.employeeNumber = employeeNumber;
		this.name = name;
		this.email = email;
		this.roleTitle = roleTitle;
		this.requestedTeamCode = requestedTeamCode;
		this.requestedTeamName = requestedTeamName;
		this.userRole = userRole;
		this.passwordHash = passwordHash;
		this.requestStatus = requestStatus;
		this.reviewNote = reviewNote;
		this.createdAt = createdAt;
	}

	public void review(SignupRequestStatus requestStatus, String reviewNote, UserEntity reviewedByUser, LocalDateTime reviewedAt) {
		this.requestStatus = requestStatus;
		this.reviewNote = reviewNote;
		this.reviewedByUser = reviewedByUser;
		this.reviewedAt = reviewedAt;
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

	public String getRequestedTeamCode() {
		return requestedTeamCode;
	}

	public String getRequestedTeamName() {
		return requestedTeamName;
	}

	public UserRole getUserRole() {
		return userRole;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public SignupRequestStatus getRequestStatus() {
		return requestStatus;
	}

	public String getReviewNote() {
		return reviewNote;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getReviewedAt() {
		return reviewedAt;
	}

	public UserEntity getReviewedByUser() {
		return reviewedByUser;
	}
}
