package com.juteak.collaboration.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Individual answers keep the thread history visible and allow one accepted resolution to be marked later.
 */
@Entity
@Table(name = "collab_question_answer")
public class QuestionAnswerEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_thread_id", nullable = false)
	private QuestionThreadEntity questionThread;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "answered_by_id", nullable = false)
	private UserEntity answeredBy;

	@Lob
	@Column(name = "answer_body")
	private String answerBody;

	@Column(name = "accepted", nullable = false)
	private boolean accepted;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected QuestionAnswerEntity() {
	}

	public QuestionAnswerEntity(
		QuestionThreadEntity questionThread,
		UserEntity answeredBy,
		String answerBody,
		boolean accepted,
		LocalDateTime createdAt
	) {
		this.questionThread = questionThread;
		this.answeredBy = answeredBy;
		this.answerBody = answerBody;
		this.accepted = accepted;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public QuestionThreadEntity getQuestionThread() {
		return questionThread;
	}

	public UserEntity getAnsweredBy() {
		return answeredBy;
	}

	public String getAnswerBody() {
		return answerBody;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
