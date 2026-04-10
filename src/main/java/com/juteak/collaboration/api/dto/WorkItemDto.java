package com.juteak.collaboration.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.juteak.collaboration.persistence.entity.WorkPriority;
import com.juteak.collaboration.persistence.entity.WorkStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Work item DTOs define the collaboration MVP's main storage contract.
 */
public final class WorkItemDto {

	private WorkItemDto() {
	}

	public record CreateRequest(
		@NotBlank(message = "workCode is required")
		@Size(max = 64, message = "workCode must be 64 characters or fewer")
		String workCode,

		@NotBlank(message = "title is required")
		String title,

		String summary,

		@NotBlank(message = "owningTeamCode is required")
		String owningTeamCode,

		String primaryAssigneeEmployeeNumber,
		String backupAssigneeEmployeeNumber,

		@NotNull(message = "workStatus is required")
		WorkStatus workStatus,

		@NotNull(message = "priority is required")
		WorkPriority priority,

		LocalDate dueDate,
		boolean criticalKnowledgeRisk,
		boolean needsHandover,
		LocalDateTime lastReviewedAt
	) {
	}

	public record UpdateRequest(
		@NotBlank(message = "title is required")
		String title,

		String summary,

		@NotBlank(message = "owningTeamCode is required")
		String owningTeamCode,

		String primaryAssigneeEmployeeNumber,
		String backupAssigneeEmployeeNumber,

		@NotNull(message = "workStatus is required")
		WorkStatus workStatus,

		@NotNull(message = "priority is required")
		WorkPriority priority,

		LocalDate dueDate,
		boolean criticalKnowledgeRisk,
		boolean needsHandover,
		LocalDateTime lastReviewedAt
	) {
	}

	public record Response(
		Long id,
		String workCode,
		String title,
		String summary,
		String owningTeamCode,
		String owningTeamName,
		String primaryAssigneeEmployeeNumber,
		String primaryAssigneeName,
		String backupAssigneeEmployeeNumber,
		String backupAssigneeName,
		WorkStatus workStatus,
		WorkPriority priority,
		LocalDate dueDate,
		boolean criticalKnowledgeRisk,
		boolean needsHandover,
		LocalDateTime lastReviewedAt
	) {
	}
}
