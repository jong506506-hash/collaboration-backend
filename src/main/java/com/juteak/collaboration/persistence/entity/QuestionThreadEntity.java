package com.juteak.collaboration.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A question thread captures who asked, who was targeted, and who ultimately handled the request.
 */
@Entity
@Table(name = "collab_question_thread")
public class QuestionThreadEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requested_by_id", nullable = false)
	private UserEntity requestedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_team_id")
	private TeamEntity targetTeam;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_user_id")
	private UserEntity targetUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "routed_user_id")
	private UserEntity routedUser;

	@Enumerated(EnumType.STRING)
	@Column(name = "question_status", nullable = false, length = 32)
	private QuestionStatus questionStatus;

	@Column(name = "title", nullable = false)
	private String title;

	@Lob
	@Column(name = "question_body")
	private String questionBody;

	@Lob
	@Column(name = "resolution_summary")
	private String resolutionSummary;

	@Column(name = "source_work_code", length = 64)
	private String sourceWorkCode;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected QuestionThreadEntity() {
	}

	public QuestionThreadEntity(
		UserEntity requestedBy,
		TeamEntity targetTeam,
		UserEntity targetUser,
		UserEntity routedUser,
		QuestionStatus questionStatus,
		String title,
		String questionBody,
		String resolutionSummary,
		String sourceWorkCode,
		LocalDateTime createdAt
	) {
		this.requestedBy = requestedBy;
		this.targetTeam = targetTeam;
		this.targetUser = targetUser;
		this.routedUser = routedUser;
		this.questionStatus = questionStatus;
		this.title = title;
		this.questionBody = questionBody;
		this.resolutionSummary = resolutionSummary;
		this.sourceWorkCode = sourceWorkCode;
		this.createdAt = createdAt;
	}

	public void updateStatus(QuestionStatus questionStatus, String resolutionSummary, UserEntity routedUser) {
		this.questionStatus = questionStatus;
		this.resolutionSummary = resolutionSummary;
		this.routedUser = routedUser;
	}

	public Long getId() {
		return id;
	}

	public UserEntity getRequestedBy() {
		return requestedBy;
	}

	public TeamEntity getTargetTeam() {
		return targetTeam;
	}

	public UserEntity getTargetUser() {
		return targetUser;
	}

	public UserEntity getRoutedUser() {
		return routedUser;
	}

	public QuestionStatus getQuestionStatus() {
		return questionStatus;
	}

	public String getTitle() {
		return title;
	}

	public String getQuestionBody() {
		return questionBody;
	}

	public String getResolutionSummary() {
		return resolutionSummary;
	}

	public String getSourceWorkCode() {
		return sourceWorkCode;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
