package com.juteak.collaboration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.api.UserController;
import com.juteak.collaboration.api.UserStatusController;
import com.juteak.collaboration.api.WorkItemController;
import com.juteak.collaboration.api.dto.TeamDto;
import com.juteak.collaboration.api.dto.UserDto;
import com.juteak.collaboration.api.dto.UserStatusDto;
import com.juteak.collaboration.api.dto.WorkItemDto;
import com.juteak.collaboration.application.TeamService;
import com.juteak.collaboration.application.UserService;
import com.juteak.collaboration.persistence.entity.UserAvailabilityStatus;
import com.juteak.collaboration.persistence.entity.UserRole;
import com.juteak.collaboration.persistence.entity.WorkPriority;
import com.juteak.collaboration.persistence.entity.WorkStatus;

/**
 * Exercises the controller-level role guards without adding extra test dependencies.
 */
@SpringBootTest
class AccessControlIntegrationTests {

	@Autowired
	private TeamService teamService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserController userController;

	@Autowired
	private UserStatusController userStatusController;

	@Autowired
	private WorkItemController workItemController;

	@Test
	void memberCannotCreateUserButCanUpdateOwnStatus() {
		teamService.createTeam(new TeamDto.CreateRequest("ACL", "Access Team", "Owns permissions", true));

		UserDto.Response member = userService.createUser(
			new UserDto.CreateRequest("ACL-M1", "Access Member", "acl.member@example.com", "Analyst", "pw-member", UserRole.MEMBER, "ACL", true)
		);

		UserDto.CreateRequest createRequest = new UserDto.CreateRequest(
			"ACL-M2",
			"Another Member",
			"acl.member2@example.com",
			"Analyst",
			"pw-member-2",
			UserRole.MEMBER,
			"ACL",
			true
		);

		assertThatThrownBy(() -> userController.createUser(member.employeeNumber(), createRequest))
			.isInstanceOf(ResponseStatusException.class)
			.extracting(error -> ((ResponseStatusException) error).getStatusCode())
			.isEqualTo(HttpStatus.FORBIDDEN);

		UserStatusDto.Response status = userStatusController.upsertStatus(
			member.employeeNumber(),
			member.id(),
			new UserStatusDto.UpsertRequest(
				UserAvailabilityStatus.AWAY,
				"Reviewing docs",
				null,
				null,
				null
			)
		);

		assertThat(status.availabilityStatus()).isEqualTo(UserAvailabilityStatus.AWAY);
	}

	@Test
	void managerCanCreateWorkItem() {
		teamService.createTeam(new TeamDto.CreateRequest("ACL-W", "Access Work Team", "Owns work permissions", true));

		UserDto.Response manager = userService.createUser(
			new UserDto.CreateRequest("ACL-G1", "Access Manager", "acl.manager@example.com", "Manager", "pw-manager", UserRole.MANAGER, "ACL-W", true)
		);

		WorkItemDto.Response workItem = workItemController.createWorkItem(
			manager.employeeNumber(),
			new WorkItemDto.CreateRequest(
				"ACL-WORK-1",
				"Permission ready work item",
				"Created by manager level access",
				"ACL-W",
				"ACL-G1",
				null,
				WorkStatus.IN_PROGRESS,
				WorkPriority.MEDIUM,
				null,
				false,
				false,
				null
			)
		);

		assertThat(workItem.workCode()).isEqualTo("ACL-WORK-1");
	}
}
