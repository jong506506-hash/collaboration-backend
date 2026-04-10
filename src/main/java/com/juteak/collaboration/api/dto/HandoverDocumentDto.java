package com.juteak.collaboration.api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

/**
 * Handover document DTOs keep the authoring contract explicit for the frontend.
 */
public final class HandoverDocumentDto {

	private HandoverDocumentDto() {
	}

	public record CreateRequest(
		@NotBlank(message = "workCode is required")
		String workCode,

		@NotBlank(message = "authorEmployeeNumber is required")
		String authorEmployeeNumber,

		@NotBlank(message = "title is required")
		String title,

		String currentStateSummary,
		String pendingActions,
		String relatedSystems,
		String keyContacts,
		String handoverNotes,
		String riskNotes,
		LocalDateTime lastVerifiedAt
	) {
	}

	public record UpdateRequest(
		@NotBlank(message = "authorEmployeeNumber is required")
		String authorEmployeeNumber,

		@NotBlank(message = "title is required")
		String title,

		String currentStateSummary,
		String pendingActions,
		String relatedSystems,
		String keyContacts,
		String handoverNotes,
		String riskNotes,
		LocalDateTime lastVerifiedAt
	) {
	}

	public record Response(
		Long id,
		String workCode,
		String workTitle,
		String authorEmployeeNumber,
		String authorName,
		String title,
		String currentStateSummary,
		String pendingActions,
		String relatedSystems,
		String keyContacts,
		String handoverNotes,
		String riskNotes,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		LocalDateTime lastVerifiedAt
	) {
	}
}
