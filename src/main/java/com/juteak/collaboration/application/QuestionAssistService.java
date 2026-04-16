package com.juteak.collaboration.application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juteak.collaboration.api.dto.QuestionAssistDto;
import com.juteak.collaboration.api.dto.UnifiedSearchDto;

/**
 * Question assist service reshapes raw retrieval results into human-readable guidance and can
 * optionally upgrade the deterministic draft with an OpenAI-backed answer.
 */
@Service
@Transactional(readOnly = true)
public class QuestionAssistService {

	private final UnifiedSearchService unifiedSearchService;
	private final OpenAiQuestionDraftService openAiQuestionDraftService;

	public QuestionAssistService(
		UnifiedSearchService unifiedSearchService,
		OpenAiQuestionDraftService openAiQuestionDraftService
	) {
		this.unifiedSearchService = unifiedSearchService;
		this.openAiQuestionDraftService = openAiQuestionDraftService;
	}

	public QuestionAssistDto.ContextResponse buildContext(
		String actorEmployeeNumber,
		QuestionAssistDto.ContextRequest request
	) {
		String query = Stream.of(request.title(), request.questionBody(), request.sourceWorkCode())
			.filter(value -> value != null && !value.isBlank())
			.map(String::trim)
			.distinct()
			.reduce((left, right) -> left + " " + right)
			.orElse("");

		UnifiedSearchDto.Response searchResult = unifiedSearchService.searchAll(actorEmployeeNumber, query);
		List<String> suggestedContext = buildSuggestedContext(searchResult);
		QuestionAssistDto.DraftResponse deterministicDraft = buildDeterministicDraft(searchResult);
		QuestionAssistDto.DraftResponse draft = openAiQuestionDraftService
			.generateDraft(query, request, searchResult, suggestedContext)
			.orElse(deterministicDraft);

		return new QuestionAssistDto.ContextResponse(
			query,
			buildSummary(searchResult),
			suggestedContext,
			draft,
			searchResult
		);
	}

	private String buildSummary(UnifiedSearchDto.Response searchResult) {
		if (searchResult.totalCount() == 0) {
			return "아직 바로 참고할 사내 문서를 찾지 못했습니다. 질문을 등록하면 담당자에게 바로 전달할 수 있습니다.";
		}

		List<String> matchedBuckets = new ArrayList<>();
		if (!searchResult.personalWorkspaces().isEmpty()) {
			matchedBuckets.add("개인 업무 공간 " + searchResult.personalWorkspaces().size() + "건");
		}
		if (!searchResult.workItems().isEmpty()) {
			matchedBuckets.add("업무 카드 " + searchResult.workItems().size() + "건");
		}
		if (!searchResult.handoverDocuments().isEmpty()) {
			matchedBuckets.add("인수인계 문서 " + searchResult.handoverDocuments().size() + "건");
		}
		if (!searchResult.questionThreads().isEmpty()) {
			matchedBuckets.add("유사 질문 " + searchResult.questionThreads().size() + "건");
		}
		if (!searchResult.teams().isEmpty()) {
			matchedBuckets.add("관련 팀 " + searchResult.teams().size() + "건");
		}
		if (!searchResult.users().isEmpty()) {
			matchedBuckets.add("관련 사용자 " + searchResult.users().size() + "건");
		}

		return "질문 전에 먼저 볼 만한 자료로 " + String.join(", ", matchedBuckets) + "이 검색되었습니다.";
	}

	private List<String> buildSuggestedContext(UnifiedSearchDto.Response searchResult) {
		List<String> suggestions = new ArrayList<>();

		if (!searchResult.personalWorkspaces().isEmpty()) {
			UnifiedSearchDto.PersonalWorkspaceResult workspace = searchResult.personalWorkspaces().getFirst();
			if (workspace.faqPreview() != null && !workspace.faqPreview().isBlank()) {
				suggestions.add(workspace.ownerName() + " 업무 공간 FAQ에서 관련 답변 흔적을 찾았습니다: " + workspace.faqPreview());
			} else {
				suggestions.add(workspace.ownerName() + " 업무 공간에는 관련 역할, 절차, 주의사항이 정리되어 있습니다.");
			}
		}

		if (!searchResult.workItems().isEmpty()) {
			UnifiedSearchDto.WorkItemResult workItem = searchResult.workItems().getFirst();
			suggestions.add("관련 업무 카드 '" + workItem.title() + " (" + workItem.workCode() + ")'를 먼저 확인해 보세요.");
		}

		if (!searchResult.handoverDocuments().isEmpty()) {
			UnifiedSearchDto.HandoverDocumentResult handoverDocument = searchResult.handoverDocuments().getFirst();
			suggestions.add("인수인계 문서 '" + handoverDocument.title() + "'에 현재 상태와 다음 액션이 정리되어 있습니다.");
		}

		if (!searchResult.questionThreads().isEmpty()) {
			UnifiedSearchDto.QuestionThreadResult questionThread = searchResult.questionThreads().getFirst();
			suggestions.add("과거 유사 질문 '" + questionThread.title() + "'를 참고하면 답변 맥락을 빨리 파악할 수 있습니다.");
		}

		if (suggestions.isEmpty()) {
			suggestions.add("현재 질문으로는 설명형 문서 맥락이 부족합니다. 필요하면 바로 질문을 등록해 담당자 확인을 받아보세요.");
		}

		return suggestions;
	}

	private QuestionAssistDto.DraftResponse buildDeterministicDraft(UnifiedSearchDto.Response searchResult) {
		List<String> sources = new ArrayList<>();
		List<String> reasoning = new ArrayList<>();

		UnifiedSearchDto.PersonalWorkspaceResult workspace = searchResult.personalWorkspaces().stream().findFirst().orElse(null);
		UnifiedSearchDto.WorkItemResult workItem = searchResult.workItems().stream().findFirst().orElse(null);
		UnifiedSearchDto.HandoverDocumentResult handoverDocument = searchResult.handoverDocuments().stream().findFirst().orElse(null);
		UnifiedSearchDto.QuestionThreadResult similarQuestion = searchResult.questionThreads().stream().findFirst().orElse(null);

		if (workspace != null) {
			sources.add("개인 업무 공간: " + workspace.title());
			if (workspace.faqPreview() != null && !workspace.faqPreview().isBlank()) {
				reasoning.add("업무 공간 FAQ에는 \"" + workspace.faqPreview() + "\" 같은 답변 흔적이 있어, 이미 비슷한 문의가 처리된 적이 있을 가능성이 높습니다.");
			} else {
				reasoning.add(workspace.ownerName() + " 업무 공간에서 관련 역할과 절차 메모를 먼저 확인하는 것이 좋습니다.");
			}
		}

		if (workItem != null) {
			sources.add("업무 카드: " + workItem.title() + " (" + workItem.workCode() + ")");
			reasoning.add("관련 업무 카드는 현재 상태가 " + workItem.workStatus() + "이고 우선순위는 " + workItem.priority() + "로 관리되고 있습니다.");
		}

		if (handoverDocument != null) {
			sources.add("인수인계 문서: " + handoverDocument.title());
			reasoning.add("인수인계 문서에는 현재 업무 맥락과 다음 담당자가 봐야 할 포인트가 들어 있을 가능성이 높습니다.");
		}

		if (similarQuestion != null) {
			sources.add("유사 질문: " + similarQuestion.title());
			reasoning.add("과거 유사 질문이 이미 존재하므로 같은 질문을 다시 보내기 전에 기존 답변을 먼저 참고해 볼 수 있습니다.");
		}

		if (sources.isEmpty()) {
			return new QuestionAssistDto.DraftResponse(
				"document-heuristic",
				"문서 기반 답변 초안을 만들기에는 근거가 부족합니다.",
				"현재 검색된 사내 문서가 충분하지 않아 신뢰할 만한 초안을 만들지 못했습니다. 이 경우에는 질문을 등록해 담당자 확인을 받는 편이 안전합니다.",
				List.of(),
				true
			);
		}

		boolean needsHumanFollowUp = workspace == null || (workspace.faqPreview() == null || workspace.faqPreview().isBlank());
		String headline;
		if (workspace != null && workspace.faqPreview() != null && !workspace.faqPreview().isBlank()) {
			headline = "이미 쌓인 FAQ 답변을 먼저 참고하면 바로 해결될 가능성이 높습니다.";
		} else if (handoverDocument != null || similarQuestion != null) {
			headline = "관련 문서를 먼저 보면 담당자 답변 없이도 방향을 잡을 수 있습니다.";
		} else {
			headline = "업무 카드와 문서를 먼저 확인한 뒤, 부족하면 담당자 질문으로 넘기는 편이 좋습니다.";
		}

		StringBuilder body = new StringBuilder();
		body.append("현재 검색된 사내 문서를 기준으로 보면, 아래 순서로 확인하는 것이 가장 효율적입니다.\n\n");
		for (int index = 0; index < reasoning.size(); index++) {
			body.append(index + 1).append(". ").append(reasoning.get(index)).append('\n');
		}
		body.append('\n');
		if (needsHumanFollowUp) {
			body.append("문서만으로 바로 확정하기 어려울 수 있으니, 위 자료를 본 뒤에도 불명확하면 담당자에게 질문을 보내는 것이 안전합니다.");
		} else {
			body.append("문서와 FAQ만으로도 1차 답을 잡을 수 있어 보입니다. 그래도 예외 상황이 있으면 마지막에 담당자에게 확인을 요청하면 됩니다.");
		}

		return new QuestionAssistDto.DraftResponse(
			"document-heuristic",
			headline,
			body.toString(),
			sources,
			needsHumanFollowUp
		);
	}
}
