package com.juteak.collaboration.api;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juteak.collaboration.api.dto.QuestionAssistDto;
import com.juteak.collaboration.application.QuestionAssistService;

/**
 * Question assist endpoints expose document-first context before a question is sent to a person.
 */
@Validated
@RestController
@RequestMapping("/api/question-assist")
public class QuestionAssistController {

	private final QuestionAssistService questionAssistService;

	public QuestionAssistController(QuestionAssistService questionAssistService) {
		this.questionAssistService = questionAssistService;
	}

	@PostMapping("/context")
	public QuestionAssistDto.ContextResponse buildContext(
		@RequestHeader("X-Employee-Number") String actorEmployeeNumber,
		@RequestBody QuestionAssistDto.ContextRequest request
	) {
		return questionAssistService.buildContext(actorEmployeeNumber, request);
	}
}
