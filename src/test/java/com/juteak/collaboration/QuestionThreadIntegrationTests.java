package com.juteak.collaboration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.juteak.collaboration.api.dto.NotificationDto;
import com.juteak.collaboration.api.dto.QuestionThreadDto;
import com.juteak.collaboration.api.dto.TeamDto;
import com.juteak.collaboration.api.dto.UserDto;
import com.juteak.collaboration.api.dto.UserStatusDto;
import com.juteak.collaboration.application.NotificationService;
import com.juteak.collaboration.application.QuestionThreadService;
import com.juteak.collaboration.application.TeamService;
import com.juteak.collaboration.application.UserService;
import com.juteak.collaboration.application.UserStatusService;
import com.juteak.collaboration.persistence.entity.QuestionStatus;
import com.juteak.collaboration.persistence.entity.UserAvailabilityStatus;
import com.juteak.collaboration.persistence.entity.UserRole;

/**
 * Verifies that questions can be created, rerouted to a substitute, and answered.
 */
@SpringBootTest
class QuestionThreadIntegrationTests {

	@Autowired
	private TeamService teamService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserStatusService userStatusService;

	@Autowired
	private QuestionThreadService questionThreadService;

	@Autowired
	private NotificationService notificationService;

	@Test
	void createQuestionAndRouteToSubstituteWhenTargetIsUnavailable() {
		teamService.createTeam(new TeamDto.CreateRequest("OPS", "Operations Team", "Handles operations", true));

		UserDto.Response requester = userService.createUser(
			new UserDto.CreateRequest("REQ-1", "Requester", "req@example.com", "Analyst", "pw-req", UserRole.MEMBER, "OPS", true)
		);
		UserDto.Response target = userService.createUser(
			new UserDto.CreateRequest("TGT-1", "Target", "target@example.com", "Operator", "pw-tgt", UserRole.MEMBER, "OPS", true)
		);
		UserDto.Response substitute = userService.createUser(
			new UserDto.CreateRequest("SUB-1", "Substitute", "sub@example.com", "Backup Operator", "pw-sub", UserRole.MEMBER, "OPS", true)
		);

		userStatusService.upsertStatus(
			target.id(),
			new UserStatusDto.UpsertRequest(
				UserAvailabilityStatus.IN_MEETING,
				"In a long meeting",
				substitute.employeeNumber(),
				null,
				null
			)
		);

		QuestionThreadDto.DetailResponse created = questionThreadService.createQuestionThread(
			new QuestionThreadDto.CreateRequest(
				requester.employeeNumber(),
				"OPS",
				target.employeeNumber(),
				"Need an answer about operations",
				"Who should process today's urgent request?",
				null
			)
		);

		assertThat(created.routedUserEmployeeNumber()).isEqualTo("SUB-1");
		assertThat(created.questionStatus()).isEqualTo(QuestionStatus.OPEN);
		assertThat(notificationService.getNotifications("TGT-1")).hasSize(1);
		assertThat(notificationService.getNotifications("SUB-1")).hasSize(1);

		QuestionThreadDto.AnswerResponse answer = questionThreadService.addAnswer(
			created.id(),
			new QuestionThreadDto.AnswerCreateRequest(
				substitute.employeeNumber(),
				"I will handle the request today.",
				true,
				"Temporary ownership moved to substitute."
			)
		);

		assertThat(answer.answeredByEmployeeNumber()).isEqualTo("SUB-1");

		QuestionThreadDto.DetailResponse fetched = questionThreadService.getQuestionThread(created.id());
		assertThat(fetched.answers()).hasSize(1);
		assertThat(fetched.questionStatus()).isEqualTo(QuestionStatus.ANSWERED);
		assertThat(fetched.resolutionSummary()).isEqualTo("Temporary ownership moved to substitute.");
		assertThat(notificationService.getNotifications("REQ-1")).extracting(NotificationDto.Response::title)
			.contains("질문에 새 답변이 등록되었습니다.");
	}
}
