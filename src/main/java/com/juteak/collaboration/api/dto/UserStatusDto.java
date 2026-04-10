package com.juteak.collaboration.api.dto;

import java.time.LocalDateTime;

import com.juteak.collaboration.persistence.entity.UserAvailabilityStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * User status DTOs model one current availability record per user.
 */
public final class UserStatusDto {

	private UserStatusDto() {
	}

	public record UpsertRequest(
		@NotNull(message = "availabilityStatus is required")
		UserAvailabilityStatus availabilityStatus,

		@Size(max = 500, message = "statusMessage must be 500 characters or fewer")
		String statusMessage,

		String substituteEmployeeNumber,
		LocalDateTime availableFrom,
		LocalDateTime availableUntil
	) {
	}

	public record Response(
		Long id,
		Long userId,
		String employeeNumber,
		String userName,
		UserAvailabilityStatus availabilityStatus,
		String statusMessage,
		String substituteEmployeeNumber,
		String substituteUserName,
		LocalDateTime availableFrom,
		LocalDateTime availableUntil,
		LocalDateTime lastUpdatedAt
	) {
	}
}
