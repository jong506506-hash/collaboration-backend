package com.juteak.collaboration.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.persistence.entity.UserEntity;
import com.juteak.collaboration.persistence.entity.UserRole;

/**
 * Centralizes lightweight role checks so the MVP can grow consistent access rules.
 */
@Service
@Transactional(readOnly = true)
public class AccessControlService {

	private final UserService userService;

	public AccessControlService(UserService userService) {
		this.userService = userService;
	}

	public UserEntity requireAdmin(String employeeNumber) {
		UserEntity actor = getRequiredActor(employeeNumber);
		if (actor.getUserRole() != UserRole.ADMIN) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role is required.");
		}
		return actor;
	}

	public UserEntity requireManagerOrAdmin(String employeeNumber) {
		UserEntity actor = getRequiredActor(employeeNumber);
		if (actor.getUserRole() != UserRole.ADMIN && actor.getUserRole() != UserRole.MANAGER) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Manager or admin role is required.");
		}
		return actor;
	}

	public UserEntity requireSelfOrManagerOrAdmin(String employeeNumber, Long targetUserId) {
		UserEntity actor = getRequiredActor(employeeNumber);
		if (actor.getUserRole() == UserRole.ADMIN || actor.getUserRole() == UserRole.MANAGER || actor.getId().equals(targetUserId)) {
			return actor;
		}
		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can update only your own status.");
	}

	private UserEntity getRequiredActor(String employeeNumber) {
		if (employeeNumber == null || employeeNumber.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-Employee-Number header is required.");
		}
		return userService.getRequiredUser(employeeNumber);
	}
}
