package com.juteak.collaboration.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juteak.collaboration.api.dto.UserStatusDto;
import com.juteak.collaboration.persistence.entity.UserEntity;
import com.juteak.collaboration.persistence.entity.UserStatusEntity;
import com.juteak.collaboration.persistence.repository.UserStatusRepository;

/**
 * User status service manages the single active availability record per user.
 */
@Service
@Transactional
public class UserStatusService {

	private final UserStatusRepository userStatusRepository;
	private final UserService userService;

	public UserStatusService(UserStatusRepository userStatusRepository, UserService userService) {
		this.userStatusRepository = userStatusRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<UserStatusDto.Response> getStatuses() {
		return userStatusRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public UserStatusDto.Response getStatus(Long userId) {
		return toResponse(getRequiredStatus(userId));
	}

	public UserStatusDto.Response upsertStatus(Long userId, UserStatusDto.UpsertRequest request) {
		UserEntity user = userService.getRequiredUser(userId);
		UserEntity substituteUser = resolveSubstitute(request.substituteEmployeeNumber());
		LocalDateTime now = LocalDateTime.now();

		UserStatusEntity status = userStatusRepository.findByUserId(userId)
			.map(existing -> {
				existing.update(
					request.availabilityStatus(),
					request.statusMessage(),
					substituteUser,
					request.availableFrom(),
					request.availableUntil(),
					now
				);
				return existing;
			})
			.orElseGet(() -> new UserStatusEntity(
				user,
				request.availabilityStatus(),
				request.statusMessage(),
				substituteUser,
				request.availableFrom(),
				request.availableUntil(),
				now
			));

		return toResponse(userStatusRepository.save(status));
	}

	private UserStatusEntity getRequiredStatus(Long userId) {
		return userStatusRepository.findByUserId(userId)
			.orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
				org.springframework.http.HttpStatus.NOT_FOUND,
				"User status not found for userId: " + userId
			));
	}

	private UserEntity resolveSubstitute(String substituteEmployeeNumber) {
		if (substituteEmployeeNumber == null || substituteEmployeeNumber.isBlank()) {
			return null;
		}
		return userService.getRequiredUser(substituteEmployeeNumber);
	}

	private UserStatusDto.Response toResponse(UserStatusEntity status) {
		UserEntity substitute = status.getSubstituteUser();
		return new UserStatusDto.Response(
			status.getId(),
			status.getUser().getId(),
			status.getUser().getEmployeeNumber(),
			status.getUser().getName(),
			status.getAvailabilityStatus(),
			status.getStatusMessage(),
			substitute == null ? null : substitute.getEmployeeNumber(),
			substitute == null ? null : substitute.getName(),
			status.getAvailableFrom(),
			status.getAvailableUntil(),
			status.getLastUpdatedAt()
		);
	}
}
