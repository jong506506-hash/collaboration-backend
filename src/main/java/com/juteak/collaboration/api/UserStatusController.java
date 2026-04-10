package com.juteak.collaboration.api;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juteak.collaboration.api.dto.UserStatusDto;
import com.juteak.collaboration.application.AccessControlService;
import com.juteak.collaboration.application.UserStatusService;

import jakarta.validation.Valid;

/**
 * User status endpoints expose the current availability state that drives rerouting decisions.
 */
@Validated
@RestController
@RequestMapping("/api/user-statuses")
public class UserStatusController {

	private final UserStatusService userStatusService;
	private final AccessControlService accessControlService;

	public UserStatusController(UserStatusService userStatusService, AccessControlService accessControlService) {
		this.userStatusService = userStatusService;
		this.accessControlService = accessControlService;
	}

	@GetMapping
	public List<UserStatusDto.Response> getStatuses() {
		return userStatusService.getStatuses();
	}

	@GetMapping("/{userId}")
	public UserStatusDto.Response getStatus(@PathVariable Long userId) {
		return userStatusService.getStatus(userId);
	}

	@PutMapping("/{userId}")
	public UserStatusDto.Response upsertStatus(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@PathVariable Long userId,
		@Valid @RequestBody UserStatusDto.UpsertRequest request
	) {
		accessControlService.requireSelfOrManagerOrAdmin(actorEmployeeNumber, userId);
		return userStatusService.upsertStatus(userId, request);
	}
}
