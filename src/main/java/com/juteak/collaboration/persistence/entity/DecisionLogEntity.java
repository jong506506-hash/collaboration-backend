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
 * Decision logs record why a work item moved in a certain direction so future owners understand the context.
 */
@Entity
@Table(name = "collab_decision_log")
public class DecisionLogEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "work_item_id", nullable = false)
	private WorkItemEntity workItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private UserEntity author;

	@Column(name = "decision_title", nullable = false)
	private String decisionTitle;

	@Lob
	@Column(name = "decision_context")
	private String decisionContext;

	@Lob
	@Column(name = "decision_outcome")
	private String decisionOutcome;

	@Lob
	@Column(name = "follow_up_action")
	private String followUpAction;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected DecisionLogEntity() {
	}
}
