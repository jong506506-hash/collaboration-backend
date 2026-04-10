package com.juteak.collaboration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.juteak.collaboration.api.dto.SignupRequestDto;
import com.juteak.collaboration.api.dto.TeamDto;
import com.juteak.collaboration.application.SignupRequestService;
import com.juteak.collaboration.application.TeamService;
import com.juteak.collaboration.application.UserService;
import com.juteak.collaboration.persistence.entity.SignupRequestStatus;
import com.juteak.collaboration.persistence.entity.UserRole;

/**
 * Proves that a user can request signup and an admin can approve the account.
 */
@SpringBootTest
class SignupRequestIntegrationTests {

	@Autowired
	private SignupRequestService signupRequestService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private UserService userService;

	@Test
	void createAndApproveSignupRequest() {
		teamService.createTeam(new TeamDto.CreateRequest("APP", "Application Team", "Owns app delivery", true));

		SignupRequestDto.Response created = signupRequestService.createSignupRequest(
			new SignupRequestDto.CreateRequest(
				"NEW-1",
				"New Member",
				"new.member@example.com",
				"Engineer",
				"APP",
				"Application Team",
				"pw-signup"
			)
		);

		assertThat(created.requestStatus()).isEqualTo(SignupRequestStatus.PENDING);
		assertThat(signupRequestService.getPendingSignupRequests("ADMIN")).hasSizeGreaterThanOrEqualTo(1);

		SignupRequestDto.Response approved = signupRequestService.approveSignupRequest(
			created.id(),
			new SignupRequestDto.ReviewRequest("ADMIN", "Welcome aboard")
		);

		assertThat(approved.requestStatus()).isEqualTo(SignupRequestStatus.APPROVED);
		assertThat(userService.getUser("NEW-1").userRole()).isEqualTo(UserRole.MEMBER);
	}
}
