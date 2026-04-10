package com.juteak.collaboration.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.juteak.collaboration.persistence.entity.QuestionStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Question DTOs support the question center flow where users ask, reroute, and answer operational questions.
 */
public final class QuestionThreadDto {

	private QuestionThreadDto() {
	}

	public record CreateRequest(
		@NotBlank(message = "requestedByEmployeeNumber is required")
		String requestedByEmployeeNumber,

		String targetTeamCode,
		String targetUserEmployeeNumber,

		@NotBlank(message = "title is required")
		String title,

		@NotBlank(message = "questionBody is required")
		String questionBody,

		String sourceWorkCode
	) {
	}

	public record StatusUpdateRequest(
		@NotNull(message = "questionStatus is required")
		QuestionStatus questionStatus,

		String resolutionSummary
	) {
	}

	public record AnswerCreateRequest(
		@NotBlank(message = "answeredByEmployeeNumber is required")
		String answeredByEmployeeNumber,

		@NotBlank(message = "answerBody is required")
		String answerBody,

		boolean accepted,
		String resolutionSummary
	) {
	}

	public record SummaryResponse(
		Long id,
		String title,
		QuestionStatus questionStatus,
		String requestedByEmployeeNumber,
		String requestedByName,
		String targetTeamCode,
		String targetTeamName,
		String targetUserEmployeeNumber,
		String targetUserName,
		String routedUserEmployeeNumber,
		String routedUserName,
		String sourceWorkCode,
		LocalDateTime createdAt
	) {
	}

	public record AnswerResponse(
		Long id,
		String answeredByEmployeeNumber,
		String answeredByName,
		String answerBody,
		boolean accepted,
		LocalDateTime createdAt
	) {
	}

	public record DetailResponse(
		Long id,
		String title,
		String questionBody,
		QuestionStatus questionStatus,
		String resolutionSummary,
		String requestedByEmployeeNumber,
		String requestedByName,
		String targetTeamCode,
		String targetTeamName,
		String targetUserEmployeeNumber,
		String targetUserName,
		String routedUserEmployeeNumber,
		String routedUserName,
		String sourceWorkCode,
		LocalDateTime createdAt,
		List<AnswerResponse> answers
	) {
	}
}
