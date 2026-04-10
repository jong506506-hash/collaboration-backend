package com.juteak.collaboration.api.dto;

import java.time.LocalDateTime;

import com.juteak.collaboration.persistence.entity.SignupRequestStatus;
import com.juteak.collaboration.persistence.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Signup request DTOs support a request-then-approve internal onboarding flow.
 */
public final class SignupRequestDto {

	private SignupRequestDto() {
	}

	public record CreateRequest(
		@NotBlank(message = "employeeNumber is required")
		@Size(max = 64, message = "employeeNumber must be 64 characters or fewer")
		String employeeNumber,

		@NotBlank(message = "name is required")
		@Size(max = 100, message = "name must be 100 characters or fewer")
		String name,

		@NotBlank(message = "email is required")
		@Email(message = "email must be valid")
		String email,

		@Size(max = 128, message = "roleTitle must be 128 characters or fewer")
		String roleTitle,

		String requestedTeamCode,
		String requestedTeamName,

		@NotBlank(message = "password is required")
		@Size(min = 4, max = 100, message = "password must be between 4 and 100 characters")
		String password
	) {
	}

	public record ReviewRequest(
		@NotBlank(message = "reviewedByEmployeeNumber is required")
		String reviewedByEmployeeNumber,

		String reviewNote
	) {
	}

	public record Response(
		Long id,
		String employeeNumber,
		String name,
		String email,
		String roleTitle,
		String requestedTeamCode,
		String requestedTeamName,
		UserRole userRole,
		SignupRequestStatus requestStatus,
		String reviewNote,
		LocalDateTime createdAt,
		LocalDateTime reviewedAt,
		String reviewedByEmployeeNumber,
		String reviewedByName
	) {
	}
}
