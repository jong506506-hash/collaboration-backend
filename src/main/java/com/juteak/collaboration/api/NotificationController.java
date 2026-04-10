package com.juteak.collaboration.api;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juteak.collaboration.api.dto.NotificationDto;
import com.juteak.collaboration.application.NotificationService;

/**
 * Notification endpoints back the current user's unread inbox view.
 */
@Validated
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping("/users/{employeeNumber}")
	public List<NotificationDto.Response> getNotifications(@PathVariable String employeeNumber) {
		return notificationService.getNotifications(employeeNumber);
	}

	@PutMapping("/{notificationId}/read")
	public NotificationDto.Response markAsRead(@PathVariable Long notificationId) {
		return notificationService.markAsRead(notificationId);
	}
}
