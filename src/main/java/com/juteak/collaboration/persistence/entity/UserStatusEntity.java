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
 * Current or scheduled availability status for a user.
 * The substitute user gives the system a clear fallback contact path.
 */
@Entity
@Table(name = "collab_user_status")
public class UserStatusEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Enumerated(EnumType.STRING)
	@Column(name = "availability_status", nullable = false, length = 32)
	private UserAvailabilityStatus availabilityStatus;

	@Column(name = "status_message", length = 500)
	private String statusMessage;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "substitute_user_id")
	private UserEntity substituteUser;

	@Column(name = "available_from")
	private LocalDateTime availableFrom;

	@Column(name = "available_until")
	private LocalDateTime availableUntil;

	@Column(name = "last_updated_at", nullable = false)
	private LocalDateTime lastUpdatedAt;

	protected UserStatusEntity() {
	}

	public UserStatusEntity(
		UserEntity user,
		UserAvailabilityStatus availabilityStatus,
		String statusMessage,
		UserEntity substituteUser,
		LocalDateTime availableFrom,
		LocalDateTime availableUntil,
		LocalDateTime lastUpdatedAt
	) {
		this.user = user;
		this.availabilityStatus = availabilityStatus;
		this.statusMessage = statusMessage;
		this.substituteUser = substituteUser;
		this.availableFrom = availableFrom;
		this.availableUntil = availableUntil;
		this.lastUpdatedAt = lastUpdatedAt;
	}

	public void update(
		UserAvailabilityStatus availabilityStatus,
		String statusMessage,
		UserEntity substituteUser,
		LocalDateTime availableFrom,
		LocalDateTime availableUntil,
		LocalDateTime lastUpdatedAt
	) {
		this.availabilityStatus = availabilityStatus;
		this.statusMessage = statusMessage;
		this.substituteUser = substituteUser;
		this.availableFrom = availableFrom;
		this.availableUntil = availableUntil;
		this.lastUpdatedAt = lastUpdatedAt;
	}

	public Long getId() {
		return id;
	}

	public UserEntity getUser() {
		return user;
	}

	public UserAvailabilityStatus getAvailabilityStatus() {
		return availabilityStatus;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public UserEntity getSubstituteUser() {
		return substituteUser;
	}

	public LocalDateTime getAvailableFrom() {
		return availableFrom;
	}

	public LocalDateTime getAvailableUntil() {
		return availableUntil;
	}

	public LocalDateTime getLastUpdatedAt() {
		return lastUpdatedAt;
	}
}
