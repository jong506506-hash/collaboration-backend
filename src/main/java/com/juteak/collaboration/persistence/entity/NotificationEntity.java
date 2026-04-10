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
 * Notification records tell a specific user that a question or other collaboration event needs attention.
 */
@Entity
@Table(name = "collab_notification")
public class NotificationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type", nullable = false, length = 32)
	private NotificationType notificationType;

	@Column(name = "title", nullable = false, length = 255)
	private String title;

	@Column(name = "body")
	private String body;

	@Column(name = "read", nullable = false)
	private boolean read;

	@Column(name = "link_path", length = 255)
	private String linkPath;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected NotificationEntity() {
	}

	public NotificationEntity(
		UserEntity user,
		NotificationType notificationType,
		String title,
		String body,
		boolean read,
		String linkPath,
		LocalDateTime createdAt
	) {
		this.user = user;
		this.notificationType = notificationType;
		this.title = title;
		this.body = body;
		this.read = read;
		this.linkPath = linkPath;
		this.createdAt = createdAt;
	}

	public void markRead() {
		this.read = true;
	}

	public Long getId() {
		return id;
	}

	public UserEntity getUser() {
		return user;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}

	public boolean isRead() {
		return read;
	}

	public String getLinkPath() {
		return linkPath;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
