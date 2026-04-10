package com.juteak.collaboration.api.dto;

import java.time.LocalDateTime;

import com.juteak.collaboration.persistence.entity.NotificationType;

/**
 * Notification DTOs feed the current user's inbox and unread indicator.
 */
public final class NotificationDto {

	private NotificationDto() {
	}

	public record Response(
		Long id,
		NotificationType notificationType,
		String title,
		String body,
		boolean read,
		String linkPath,
		LocalDateTime createdAt
	) {
	}
}
