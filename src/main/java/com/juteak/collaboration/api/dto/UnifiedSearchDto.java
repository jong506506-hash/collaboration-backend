package com.juteak.collaboration.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Unified search DTOs keep the cross-domain result contract small and predictable.
 */
public final class UnifiedSearchDto {

	private UnifiedSearchDto() {
	}

	public record TeamResult(
		String teamCode,
		String name,
		String description
	) {
	}

	public record UserResult(
		String employeeNumber,
		String name,
		String email,
		String roleTitle,
		String primaryTeamName
	) {
	}

	public record WorkItemResult(
		String workCode,
		String title,
		String summary,
		String workStatus,
		String priority
	) {
	}

	public record HandoverDocumentResult(
		Long id,
		String title,
		String workCode,
		String workTitle,
		String authorName,
		LocalDateTime updatedAt
	) {
	}

	public record PersonalWorkspaceResult(
		String ownerEmployeeNumber,
		String ownerName,
		String ownerTeamName,
		String title,
		String visibilityScope,
		String faqPreview,
		LocalDateTime updatedAt
	) {
	}

	public record QuestionThreadResult(
		Long id,
		String title,
		String questionStatus,
		String requestedByName,
		String targetUserName,
		String targetTeamName,
		LocalDateTime createdAt
	) {
	}

	public record Response(
		String query,
		int totalCount,
		List<TeamResult> teams,
		List<UserResult> users,
		List<WorkItemResult> workItems,
		List<HandoverDocumentResult> handoverDocuments,
		List<PersonalWorkspaceResult> personalWorkspaces,
		List<QuestionThreadResult> questionThreads
	) {
	}
}
