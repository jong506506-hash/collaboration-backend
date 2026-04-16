package com.juteak.collaboration.api.dto;

import java.util.List;

/**
 * Question assist DTOs package retrieval context and a deterministic answer draft that can
 * later be replaced by a real LLM-backed response.
 */
public final class QuestionAssistDto {

	private QuestionAssistDto() {
	}

	public record ContextRequest(
		String title,
		String questionBody,
		String sourceWorkCode,
		String targetTeamCode,
		String targetUserEmployeeNumber
	) {
	}

	public record DraftResponse(
		String provider,
		String headline,
		String body,
		List<String> sources,
		boolean needsHumanFollowUp
	) {
	}

	public record ContextResponse(
		String query,
		String summary,
		List<String> suggestedContext,
		DraftResponse draft,
		UnifiedSearchDto.Response searchResult
	) {
	}
}
