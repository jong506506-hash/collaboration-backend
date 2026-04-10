package com.juteak.collaboration.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Team DTOs keep the HTTP contract small and explicit while the entity stays persistence-focused.
 */
public final class TeamDto {

	private TeamDto() {
	}

	public record CreateRequest(
		@NotBlank(message = "teamCode is required")
		@Size(max = 64, message = "teamCode must be 64 characters or fewer")
		String teamCode,

		@NotBlank(message = "name is required")
		@Size(max = 128, message = "name must be 128 characters or fewer")
		String name,

		@Size(max = 500, message = "description must be 500 characters or fewer")
		String description,

		boolean active
	) {
	}

	public record UpdateRequest(
		@NotBlank(message = "name is required")
		@Size(max = 128, message = "name must be 128 characters or fewer")
		String name,

		@Size(max = 500, message = "description must be 500 characters or fewer")
		String description,

		boolean active
	) {
	}

	public record Response(
		Long id,
		String teamCode,
		String name,
		String description,
		boolean active
	) {
	}
}
