package com.juteak.collaboration.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.juteak.collaboration.persistence.entity.WorkspaceVisibility;

import jakarta.validation.constraints.NotBlank;

/**
 * DTOs for a user's durable work wiki that later search and LLM layers can read from.
 */
public final class PersonalWorkspaceDto {

	private PersonalWorkspaceDto() {
	}

	public record UpsertRequest(
		@NotBlank(message = "title is required")
		String title,
		WorkspaceVisibility visibilityScope,
		String roleSummary,
		String responsibilitySummary,
		String currentFocus,
		String recurringWorkNotes,
		String faqNotes,
		String cautionNotes,
		String systemNotes,
		String referenceLinks,
		List<String> linkedWorkCodes,
		List<Long> linkedHandoverDocumentIds
	) {
	}

	public record WorkLinkSummary(
		String workCode,
		String title,
		String workStatus,
		String priority
	) {
	}

	public record HandoverLinkSummary(
		Long id,
		String title,
		String workCode,
		String workTitle
	) {
	}

	public record Response(
		Long id,
		String ownerEmployeeNumber,
		String ownerName,
		String ownerRoleTitle,
		String ownerTeamCode,
		String ownerTeamName,
		String title,
		WorkspaceVisibility visibilityScope,
		String roleSummary,
		String responsibilitySummary,
		String currentFocus,
		String recurringWorkNotes,
		String faqNotes,
		String cautionNotes,
		String systemNotes,
		String referenceLinks,
		List<WorkLinkSummary> linkedWorkItems,
		List<HandoverLinkSummary> linkedHandoverDocuments,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
	}
}
