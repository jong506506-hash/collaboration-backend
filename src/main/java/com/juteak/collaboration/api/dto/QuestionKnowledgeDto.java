package com.juteak.collaboration.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Captures useful question answers back into durable workspace knowledge.
 */
public final class QuestionKnowledgeDto {

	private QuestionKnowledgeDto() {
	}

	public record CaptureRequest(
		@NotNull(message = "answerId is required")
		Long answerId,

		@NotBlank(message = "targetWorkspaceEmployeeNumber is required")
		String targetWorkspaceEmployeeNumber
	) {
	}
}
