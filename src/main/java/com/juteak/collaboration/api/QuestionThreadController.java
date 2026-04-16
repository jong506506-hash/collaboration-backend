package com.juteak.collaboration.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.juteak.collaboration.api.dto.PersonalWorkspaceDto;
import com.juteak.collaboration.api.dto.QuestionKnowledgeDto;
import com.juteak.collaboration.api.dto.QuestionThreadDto;
import com.juteak.collaboration.application.QuestionThreadService;

import jakarta.validation.Valid;

/**
 * Question center endpoints for asking, inspecting, answering, and updating operational questions.
 */
@Validated
@RestController
@RequestMapping("/api/question-threads")
public class QuestionThreadController {

	private final QuestionThreadService questionThreadService;

	public QuestionThreadController(QuestionThreadService questionThreadService) {
		this.questionThreadService = questionThreadService;
	}

	@GetMapping
	public List<QuestionThreadDto.SummaryResponse> getQuestionThreads() {
		return questionThreadService.getQuestionThreads();
	}

	@GetMapping("/{id}")
	public QuestionThreadDto.DetailResponse getQuestionThread(@PathVariable Long id) {
		return questionThreadService.getQuestionThread(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public QuestionThreadDto.DetailResponse createQuestionThread(@Valid @RequestBody QuestionThreadDto.CreateRequest request) {
		return questionThreadService.createQuestionThread(request);
	}

	@PostMapping("/{id}/answers")
	@ResponseStatus(HttpStatus.CREATED)
	public QuestionThreadDto.AnswerResponse addAnswer(
		@PathVariable Long id,
		@Valid @RequestBody QuestionThreadDto.AnswerCreateRequest request
	) {
		return questionThreadService.addAnswer(id, request);
	}

	@PutMapping("/{id}/status")
	public QuestionThreadDto.DetailResponse updateQuestionStatus(
		@PathVariable Long id,
		@Valid @RequestBody QuestionThreadDto.StatusUpdateRequest request
	) {
		return questionThreadService.updateQuestionStatus(id, request);
	}

	@PostMapping("/{id}/knowledge-capture")
	public PersonalWorkspaceDto.Response captureAnswerAsKnowledge(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@PathVariable Long id,
		@Valid @RequestBody QuestionKnowledgeDto.CaptureRequest request
	) {
		return questionThreadService.captureAnswerAsKnowledge(
			actorEmployeeNumber,
			id,
			request.answerId(),
			request.targetWorkspaceEmployeeNumber()
		);
	}
}
