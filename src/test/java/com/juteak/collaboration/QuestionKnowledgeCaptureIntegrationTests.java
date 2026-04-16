package com.juteak.collaboration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.juteak.collaboration.api.QuestionThreadController;
import com.juteak.collaboration.api.dto.PersonalWorkspaceDto;
import com.juteak.collaboration.api.dto.QuestionKnowledgeDto;
import com.juteak.collaboration.api.dto.QuestionThreadDto;
import com.juteak.collaboration.api.dto.TeamDto;
import com.juteak.collaboration.api.dto.UserDto;
import com.juteak.collaboration.application.QuestionThreadService;
import com.juteak.collaboration.application.TeamService;
import com.juteak.collaboration.application.UserService;
import com.juteak.collaboration.persistence.entity.UserRole;

@SpringBootTest
class QuestionKnowledgeCaptureIntegrationTests {

	@Autowired
	private TeamService teamService;

	@Autowired
	private UserService userService;

	@Autowired
	private QuestionThreadService questionThreadService;

	@Autowired
	private QuestionThreadController questionThreadController;

	@Test
	void savesSelectedAnswerIntoWorkspaceFaqNotes() {
		teamService.createTeam(new TeamDto.CreateRequest("QK", "Knowledge Team", "Knowledge capture team", true));

		userService.createUser(
			new UserDto.CreateRequest("QK-REQ", "Requester", "requester@example.com", "Member", "pw-1", UserRole.MEMBER, "QK", true)
		);
		userService.createUser(
			new UserDto.CreateRequest("QK-ANS", "Answerer", "answerer@example.com", "Manager", "pw-2", UserRole.MANAGER, "QK", true)
		);

		QuestionThreadDto.DetailResponse thread = questionThreadService.createQuestionThread(
			new QuestionThreadDto.CreateRequest(
				"QK-REQ",
				"QK",
				"QK-ANS",
				"How do we prepare the report?",
				"Please share the report preparation steps.",
				null
			)
		);

		QuestionThreadDto.AnswerResponse answer = questionThreadService.addAnswer(
			thread.id(),
			new QuestionThreadDto.AnswerCreateRequest(
				"QK-ANS",
				"Start from the weekly checklist and confirm the latest handover notes.",
				true,
				"Use the weekly checklist first."
			)
		);

		PersonalWorkspaceDto.Response workspace = questionThreadController.captureAnswerAsKnowledge(
			"QK-ANS",
			thread.id(),
			new QuestionKnowledgeDto.CaptureRequest(answer.id(), "QK-ANS")
		);

		assertThat(workspace.ownerEmployeeNumber()).isEqualTo("QK-ANS");
		assertThat(workspace.faqNotes()).contains("[질문 자산화] How do we prepare the report?");
		assertThat(workspace.faqNotes()).contains("답변: Start from the weekly checklist and confirm the latest handover notes.");
		assertThat(workspace.faqNotes()).contains("해결 요약: Use the weekly checklist first.");
	}
}
