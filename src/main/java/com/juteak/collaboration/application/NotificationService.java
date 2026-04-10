package com.juteak.collaboration.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.api.dto.NotificationDto;
import com.juteak.collaboration.persistence.entity.NotificationEntity;
import com.juteak.collaboration.persistence.entity.NotificationType;
import com.juteak.collaboration.persistence.entity.UserEntity;
import com.juteak.collaboration.persistence.repository.NotificationRepository;

/**
 * Notification service writes and exposes the small inbox used by the current signed-in user.
 */
@Service
@Transactional
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserService userService;

	public NotificationService(NotificationRepository notificationRepository, UserService userService) {
		this.notificationRepository = notificationRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<NotificationDto.Response> getNotifications(String employeeNumber) {
		userService.getRequiredUser(employeeNumber);
		return notificationRepository.findByUserEmployeeNumberOrderByCreatedAtDesc(employeeNumber).stream()
			.map(this::toResponse)
			.toList();
	}

	public NotificationDto.Response markAsRead(Long notificationId) {
		NotificationEntity notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found: " + notificationId));
		notification.markRead();
		return toResponse(notification);
	}

	public void createNotification(
		UserEntity user,
		NotificationType notificationType,
		String title,
		String body,
		String linkPath
	) {
		NotificationEntity notification = new NotificationEntity(
			user,
			notificationType,
			title,
			body,
			false,
			linkPath,
			LocalDateTime.now()
		);
		notificationRepository.save(notification);
	}

	private NotificationDto.Response toResponse(NotificationEntity notification) {
		return new NotificationDto.Response(
			notification.getId(),
			notification.getNotificationType(),
			notification.getTitle(),
			notification.getBody(),
			notification.isRead(),
			notification.getLinkPath(),
			notification.getCreatedAt()
		);
	}
}
