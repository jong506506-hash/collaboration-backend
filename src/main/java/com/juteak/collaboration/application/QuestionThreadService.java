package com.juteak.collaboration.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.juteak.collaboration.api.dto.QuestionThreadDto;
import com.juteak.collaboration.persistence.entity.QuestionAnswerEntity;
import com.juteak.collaboration.persistence.entity.QuestionStatus;
import com.juteak.collaboration.persistence.entity.QuestionThreadEntity;
import com.juteak.collaboration.persistence.entity.NotificationType;
import com.juteak.collaboration.persistence.entity.TeamEntity;
import com.juteak.collaboration.persistence.entity.UserAvailabilityStatus;
import com.juteak.collaboration.persistence.entity.UserEntity;
import com.juteak.collaboration.persistence.entity.UserStatusEntity;
import com.juteak.collaboration.persistence.repository.QuestionAnswerRepository;
import com.juteak.collaboration.persistence.repository.QuestionThreadRepository;
import com.juteak.collaboration.persistence.repository.UserStatusRepository;
import com.juteak.collaboration.persistence.repository.WorkItemRepository;

/**
 * Question thread service owns the ask-answer-reroute lifecycle for the collaboration question center.
 */
@Service
@Transactional
public class QuestionThreadService {

	private final QuestionThreadRepository questionThreadRepository;
	private final QuestionAnswerRepository questionAnswerRepository;
	private final UserService userService;
	private final TeamService teamService;
	private final NotificationService notificationService;
	private final UserStatusRepository userStatusRepository;
	private final WorkItemRepository workItemRepository;

	public QuestionThreadService(
		QuestionThreadRepository questionThreadRepository,
		QuestionAnswerRepository questionAnswerRepository,
		UserService userService,
		TeamService teamService,
		NotificationService notificationService,
		UserStatusRepository userStatusRepository,
		WorkItemRepository workItemRepository
	) {
		this.questionThreadRepository = questionThreadRepository;
		this.questionAnswerRepository = questionAnswerRepository;
		this.userService = userService;
		this.teamService = teamService;
		this.notificationService = notificationService;
		this.userStatusRepository = userStatusRepository;
		this.workItemRepository = workItemRepository;
	}

	@Transactional(readOnly = true)
	public List<QuestionThreadDto.SummaryResponse> getQuestionThreads() {
		return questionThreadRepository.findAll().stream()
			.map(this::toSummaryResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public QuestionThreadDto.DetailResponse getQuestionThread(Long id) {
		QuestionThreadEntity thread = getRequiredThread(id);
		return toDetailResponse(thread, questionAnswerRepository.findByQuestionThreadIdOrderByCreatedAtAsc(id));
	}

	public QuestionThreadDto.DetailResponse createQuestionThread(QuestionThreadDto.CreateRequest request) {
		UserEntity requester = userService.getRequiredUser(request.requestedByEmployeeNumber());
		TeamEntity targetTeam = resolveTeam(request.targetTeamCode());
		UserEntity targetUser = resolveUser(request.targetUserEmployeeNumber());
		validateTarget(targetTeam, targetUser);
		validateSourceWork(request.sourceWorkCode());

		UserEntity routedUser = determineRoutedUser(targetUser);
		QuestionStatus initialStatus = determineInitialStatus(targetUser, routedUser);

		QuestionThreadEntity thread = new QuestionThreadEntity(
			requester,
			targetTeam,
			targetUser,
			routedUser,
			initialStatus,
			request.title(),
			request.questionBody(),
			null,
			request.sourceWorkCode(),
			LocalDateTime.now()
		);

		QuestionThreadEntity saved = questionThreadRepository.save(thread);
		createQuestionNotifications(saved);
		return toDetailResponse(saved, List.of());
	}

	public QuestionThreadDto.AnswerResponse addAnswer(Long threadId, QuestionThreadDto.AnswerCreateRequest request) {
		QuestionThreadEntity thread = getRequiredThread(threadId);
		UserEntity answeredBy = userService.getRequiredUser(request.answeredByEmployeeNumber());

		QuestionAnswerEntity answer = new QuestionAnswerEntity(
			thread,
			answeredBy,
			request.answerBody(),
			request.accepted(),
			LocalDateTime.now()
		);

		QuestionAnswerEntity savedAnswer = questionAnswerRepository.save(answer);

		String resolutionSummary = request.accepted()
			? request.resolutionSummary()
			: thread.getResolutionSummary();
		thread.updateStatus(QuestionStatus.ANSWERED, resolutionSummary, thread.getRoutedUser());
		notificationService.createNotification(
			thread.getRequestedBy(),
			NotificationType.QUESTION_ANSWERED,
			"질문에 새 답변이 등록되었습니다.",
			thread.getTitle(),
			"/questions/" + thread.getId()
		);

		return toAnswerResponse(savedAnswer);
	}

	public QuestionThreadDto.DetailResponse updateQuestionStatus(Long threadId, QuestionThreadDto.StatusUpdateRequest request) {
		QuestionThreadEntity thread = getRequiredThread(threadId);
		thread.updateStatus(request.questionStatus(), request.resolutionSummary(), thread.getRoutedUser());
		return toDetailResponse(thread, questionAnswerRepository.findByQuestionThreadIdOrderByCreatedAtAsc(threadId));
	}

	private QuestionThreadEntity getRequiredThread(Long id) {
		return questionThreadRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question thread not found: " + id));
	}

	private void validateTarget(TeamEntity targetTeam, UserEntity targetUser) {
		if (targetTeam == null && targetUser == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one target team or target user is required.");
		}
	}

	private void validateSourceWork(String sourceWorkCode) {
		if (sourceWorkCode == null || sourceWorkCode.isBlank()) {
			return;
		}

		workItemRepository.findByWorkCode(sourceWorkCode)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work item not found: " + sourceWorkCode));
	}

	private TeamEntity resolveTeam(String teamCode) {
		if (teamCode == null || teamCode.isBlank()) {
			return null;
		}
		return teamService.getRequiredTeam(teamCode);
	}

	private UserEntity resolveUser(String employeeNumber) {
		if (employeeNumber == null || employeeNumber.isBlank()) {
			return null;
		}
		return userService.getRequiredUser(employeeNumber);
	}

	private UserEntity determineRoutedUser(UserEntity targetUser) {
		if (targetUser == null) {
			return null;
		}

		UserStatusEntity targetStatus = userStatusRepository.findByUserId(targetUser.getId()).orElse(null);
		if (targetStatus == null || targetStatus.getAvailabilityStatus() == UserAvailabilityStatus.AVAILABLE) {
			return null;
		}

		return targetStatus.getSubstituteUser();
	}

	private QuestionStatus determineInitialStatus(UserEntity targetUser, UserEntity routedUser) {
		if (targetUser == null) {
			return QuestionStatus.OPEN;
		}

		UserStatusEntity targetStatus = userStatusRepository.findByUserId(targetUser.getId()).orElse(null);
		if (targetStatus == null || targetStatus.getAvailabilityStatus() == UserAvailabilityStatus.AVAILABLE) {
			return QuestionStatus.OPEN;
		}

		return routedUser == null ? QuestionStatus.ESCALATED : QuestionStatus.OPEN;
	}

	private void createQuestionNotifications(QuestionThreadEntity thread) {
		UserEntity targetUser = thread.getTargetUser();
		UserEntity routedUser = thread.getRoutedUser();

		if (targetUser != null && !targetUser.getEmployeeNumber().equals(thread.getRequestedBy().getEmployeeNumber())) {
			notificationService.createNotification(
				targetUser,
				NotificationType.QUESTION_ASSIGNED,
				"새 질문이 등록되었습니다.",
				thread.getTitle(),
				"/questions/" + thread.getId()
			);
		}

		if (routedUser != null && (targetUser == null || !routedUser.getEmployeeNumber().equals(targetUser.getEmployeeNumber()))) {
			notificationService.createNotification(
				routedUser,
				NotificationType.QUESTION_REROUTED,
				"부재로 인해 질문이 대체 담당자에게 전달되었습니다.",
				thread.getTitle(),
				"/questions/" + thread.getId()
			);
		}
	}

	private QuestionThreadDto.SummaryResponse toSummaryResponse(QuestionThreadEntity thread) {
		return new QuestionThreadDto.SummaryResponse(
			thread.getId(),
			thread.getTitle(),
			thread.getQuestionStatus(),
			thread.getRequestedBy().getEmployeeNumber(),
			thread.getRequestedBy().getName(),
			thread.getTargetTeam() == null ? null : thread.getTargetTeam().getTeamCode(),
			thread.getTargetTeam() == null ? null : thread.getTargetTeam().getName(),
			thread.getTargetUser() == null ? null : thread.getTargetUser().getEmployeeNumber(),
			thread.getTargetUser() == null ? null : thread.getTargetUser().getName(),
			thread.getRoutedUser() == null ? null : thread.getRoutedUser().getEmployeeNumber(),
			thread.getRoutedUser() == null ? null : thread.getRoutedUser().getName(),
			thread.getSourceWorkCode(),
			thread.getCreatedAt()
		);
	}

	private QuestionThreadDto.DetailResponse toDetailResponse(
		QuestionThreadEntity thread,
		List<QuestionAnswerEntity> answers
	) {
		return new QuestionThreadDto.DetailResponse(
			thread.getId(),
			thread.getTitle(),
			thread.getQuestionBody(),
			thread.getQuestionStatus(),
			thread.getResolutionSummary(),
			thread.getRequestedBy().getEmployeeNumber(),
			thread.getRequestedBy().getName(),
			thread.getTargetTeam() == null ? null : thread.getTargetTeam().getTeamCode(),
			thread.getTargetTeam() == null ? null : thread.getTargetTeam().getName(),
			thread.getTargetUser() == null ? null : thread.getTargetUser().getEmployeeNumber(),
			thread.getTargetUser() == null ? null : thread.getTargetUser().getName(),
			thread.getRoutedUser() == null ? null : thread.getRoutedUser().getEmployeeNumber(),
			thread.getRoutedUser() == null ? null : thread.getRoutedUser().getName(),
			thread.getSourceWorkCode(),
			thread.getCreatedAt(),
			answers.stream().map(this::toAnswerResponse).toList()
		);
	}

	private QuestionThreadDto.AnswerResponse toAnswerResponse(QuestionAnswerEntity answer) {
		return new QuestionThreadDto.AnswerResponse(
			answer.getId(),
			answer.getAnsweredBy().getEmployeeNumber(),
			answer.getAnsweredBy().getName(),
			answer.getAnswerBody(),
			answer.isAccepted(),
			answer.getCreatedAt()
		);
	}
}
