package com.juteak.collaboration.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.juteak.collaboration.api.dto.QuestionAssistDto;
import com.juteak.collaboration.api.dto.UnifiedSearchDto;

/**
 * Optional OpenAI-backed draft generator. If disabled or unavailable, the app falls back to the
 * deterministic document-only draft built in {@link QuestionAssistService}.
 */
@Service
public class OpenAiQuestionDraftService {

	private static final Logger log = LoggerFactory.getLogger(OpenAiQuestionDraftService.class);
	private static final Pattern SECTION_PATTERN = Pattern.compile(
		"(?ms)^HEADLINE:\\s*(.*?)\\s*^BODY:\\s*(.*?)\\s*^SOURCES:\\s*(.*?)\\s*^NEEDS_HUMAN_FOLLOW_UP:\\s*(true|false)\\s*$"
	);

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final boolean enabled;
	private final String apiKey;
	private final String model;

	public OpenAiQuestionDraftService(
		@Value("${app.llm.openai.enabled:false}") boolean enabled,
		@Value("${OPENAI_API_KEY:}") String apiKey,
		@Value("${OPENAI_MODEL:gpt-5-mini}") String model
	) {
		this.restClient = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
		this.objectMapper = new ObjectMapper();
		this.enabled = enabled;
		this.apiKey = apiKey;
		this.model = model;
	}

	public Optional<QuestionAssistDto.DraftResponse> generateDraft(
		String query,
		QuestionAssistDto.ContextRequest request,
		UnifiedSearchDto.Response searchResult,
		List<String> suggestedContext
	) {
		if (!enabled || !StringUtils.hasText(apiKey) || searchResult.totalCount() == 0) {
			return Optional.empty();
		}

		try {
			Map<String, Object> payload = new LinkedHashMap<>();
			payload.put("model", model);
			payload.put("instructions", """
				You are generating a concise internal answer draft for an enterprise collaboration tool.
				Use only the supplied internal document context.
				Do not invent policies, systems, or steps that are not grounded in the provided context.
				If the context is not enough for a confident answer, say so clearly and set needsHumanFollowUp to true.
				Return plain text only in exactly this format:
				HEADLINE: one short Korean sentence
				BODY: a concise Korean answer draft
				SOURCES:
				- source 1
				- source 2
				NEEDS_HUMAN_FOLLOW_UP: true or false
				""");
			payload.put("input", buildPrompt(query, request, searchResult, suggestedContext));

			String responseBody = restClient.post()
				.uri("/responses")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.header("X-Client-Request-Id", UUID.randomUUID().toString())
				.body(payload)
				.retrieve()
				.body(String.class);

			if (!StringUtils.hasText(responseBody)) {
				return Optional.empty();
			}

			JsonNode response = objectMapper.readTree(responseBody);

			String outputText = extractOutputText(response);
			if (!StringUtils.hasText(outputText)) {
				return Optional.empty();
			}

			ParsedDraft parsedDraft = parseDraft(outputText);
			return Optional.of(
				new QuestionAssistDto.DraftResponse(
					"openai:" + model,
					parsedDraft.headline(),
					parsedDraft.body(),
					parsedDraft.sources(),
					parsedDraft.needsHumanFollowUp()
				)
			);
		} catch (Exception exception) {
			log.warn("OpenAI draft generation failed, falling back to deterministic draft: {}", exception.getMessage());
			return Optional.empty();
		}
	}

	private String buildPrompt(
		String query,
		QuestionAssistDto.ContextRequest request,
		UnifiedSearchDto.Response searchResult,
		List<String> suggestedContext
	) {
		StringBuilder prompt = new StringBuilder();
		prompt.append("""
			Return plain text only in this exact layout:
			HEADLINE: ...
			BODY: ...
			SOURCES:
			- ...
			- ...
			NEEDS_HUMAN_FOLLOW_UP: true or false

			""");
		prompt.append("Question query:\n").append(query).append("\n\n");
		prompt.append("Question title:\n").append(nullSafe(request.title())).append("\n\n");
		prompt.append("Question body:\n").append(nullSafe(request.questionBody())).append("\n\n");
		prompt.append("Related work code:\n").append(nullSafe(request.sourceWorkCode())).append("\n\n");

		prompt.append("Suggested context:\n");
		for (String line : suggestedContext) {
			prompt.append("- ").append(line).append('\n');
		}
		prompt.append('\n');

		appendWorkspaceContext(prompt, searchResult.personalWorkspaces());
		appendWorkItemContext(prompt, searchResult.workItems());
		appendHandoverContext(prompt, searchResult.handoverDocuments());
		appendQuestionContext(prompt, searchResult.questionThreads());

		prompt.append("""

			Write a practical internal answer draft in Korean.
			Keep it concise but actionable.
			Prefer telling the user which internal source to check first and what confidence level the current documents support.
			When evidence is thin, explicitly recommend asking the human owner next.
			Do not return json.
			""");
		return prompt.toString();
	}

	private ParsedDraft parseDraft(String outputText) {
		Matcher matcher = SECTION_PATTERN.matcher(outputText.trim());
		if (!matcher.find()) {
			throw new IllegalArgumentException("OpenAI response did not match expected section format.");
		}

		String headline = firstNonBlank(matcher.group(1), "문서 기반 답변 초안이 준비되었습니다.");
		String body = firstNonBlank(matcher.group(2), "관련 문서를 먼저 확인해 주세요.");
		List<String> sources = new ArrayList<>();
		for (String line : matcher.group(3).split("\\R")) {
			String trimmed = line.trim();
			if (!trimmed.startsWith("-")) {
				continue;
			}
			String source = trimmed.substring(1).trim();
			if (!source.isEmpty()) {
				sources.add(source);
			}
		}

		return new ParsedDraft(headline, body, sources, Boolean.parseBoolean(matcher.group(4).trim()));
	}

	private void appendWorkspaceContext(StringBuilder prompt, List<UnifiedSearchDto.PersonalWorkspaceResult> workspaces) {
		if (workspaces.isEmpty()) {
			return;
		}
		prompt.append("Personal workspaces:\n");
		workspaces.stream().limit(3).forEach(workspace -> {
			prompt.append("- ")
				.append(workspace.title())
				.append(" / owner=")
				.append(workspace.ownerName())
				.append(" / visibility=")
				.append(workspace.visibilityScope());
			if (StringUtils.hasText(workspace.faqPreview())) {
				prompt.append(" / faq=").append(workspace.faqPreview());
			}
			prompt.append('\n');
		});
		prompt.append('\n');
	}

	private void appendWorkItemContext(StringBuilder prompt, List<UnifiedSearchDto.WorkItemResult> workItems) {
		if (workItems.isEmpty()) {
			return;
		}
		prompt.append("Work items:\n");
		workItems.stream().limit(3).forEach(workItem -> prompt.append("- ")
			.append(workItem.title())
			.append(" (")
			.append(workItem.workCode())
			.append(") / status=")
			.append(workItem.workStatus())
			.append(" / priority=")
			.append(workItem.priority())
			.append('\n'));
		prompt.append('\n');
	}

	private void appendHandoverContext(StringBuilder prompt, List<UnifiedSearchDto.HandoverDocumentResult> documents) {
		if (documents.isEmpty()) {
			return;
		}
		prompt.append("Handover documents:\n");
		documents.stream().limit(3).forEach(document -> prompt.append("- ")
			.append(document.title())
			.append(" / work=")
			.append(nullSafe(document.workCode()))
			.append(" / author=")
			.append(document.authorName())
			.append('\n'));
		prompt.append('\n');
	}

	private void appendQuestionContext(StringBuilder prompt, List<UnifiedSearchDto.QuestionThreadResult> questions) {
		if (questions.isEmpty()) {
			return;
		}
		prompt.append("Similar historical questions:\n");
		questions.stream().limit(3).forEach(question -> prompt.append("- ")
			.append(question.title())
			.append(" / status=")
			.append(question.questionStatus())
			.append('\n'));
		prompt.append('\n');
	}

	private String extractOutputText(JsonNode response) {
		if (response == null) {
			return "";
		}
		if (response.hasNonNull("output_text") && response.get("output_text").isTextual()) {
			return response.get("output_text").asText();
		}

		StringBuilder collected = new StringBuilder();
		for (JsonNode outputItem : response.path("output")) {
			for (JsonNode contentItem : outputItem.path("content")) {
				JsonNode textNode = contentItem.get("text");
				if (textNode != null && textNode.isTextual()) {
					if (!collected.isEmpty()) {
						collected.append('\n');
					}
					collected.append(textNode.asText());
				}
			}
		}
		return collected.toString();
	}

	private String nullSafe(String value) {
		return value == null ? "" : value;
	}

	private String firstNonBlank(String value, String fallback) {
		return StringUtils.hasText(value) ? value.trim() : fallback;
	}

	private record ParsedDraft(String headline, String body, List<String> sources, boolean needsHumanFollowUp) {
	}
}
