package com.juteak.collaboration.persistence.entity;

import java.time.LocalDate;
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
 * Central operational object for the new service.
 * A work item ties together owners, continuity risk, and linked knowledge.
 */
@Entity
@Table(name = "collab_work_item")
public class WorkItemEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "work_code", nullable = false, length = 64, unique = true)
	private String workCode;

	@Column(name = "title", nullable = false)
	private String title;

	@Lob
	@Column(name = "summary")
	private String summary;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owning_team_id", nullable = false)
	private TeamEntity owningTeam;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "primary_assignee_id")
	private UserEntity primaryAssignee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "backup_assignee_id")
	private UserEntity backupAssignee;

	@Enumerated(EnumType.STRING)
	@Column(name = "work_status", nullable = false, length = 32)
	private WorkStatus workStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority", nullable = false, length = 32)
	private WorkPriority priority;

	@Column(name = "due_date")
	private LocalDate dueDate;

	@Column(name = "critical_knowledge_risk", nullable = false)
	private boolean criticalKnowledgeRisk;

	@Column(name = "needs_handover", nullable = false)
	private boolean needsHandover;

	@Column(name = "last_reviewed_at")
	private LocalDateTime lastReviewedAt;

	protected WorkItemEntity() {
	}

	public WorkItemEntity(
		String workCode,
		String title,
		String summary,
		TeamEntity owningTeam,
		UserEntity primaryAssignee,
		UserEntity backupAssignee,
		WorkStatus workStatus,
		WorkPriority priority,
		LocalDate dueDate,
		boolean criticalKnowledgeRisk,
		boolean needsHandover,
		LocalDateTime lastReviewedAt
	) {
		this.workCode = workCode;
		this.title = title;
		this.summary = summary;
		this.owningTeam = owningTeam;
		this.primaryAssignee = primaryAssignee;
		this.backupAssignee = backupAssignee;
		this.workStatus = workStatus;
		this.priority = priority;
		this.dueDate = dueDate;
		this.criticalKnowledgeRisk = criticalKnowledgeRisk;
		this.needsHandover = needsHandover;
		this.lastReviewedAt = lastReviewedAt;
	}

	public void update(
		String title,
		String summary,
		TeamEntity owningTeam,
		UserEntity primaryAssignee,
		UserEntity backupAssignee,
		WorkStatus workStatus,
		WorkPriority priority,
		LocalDate dueDate,
		boolean criticalKnowledgeRisk,
		boolean needsHandover,
		LocalDateTime lastReviewedAt
	) {
		this.title = title;
		this.summary = summary;
		this.owningTeam = owningTeam;
		this.primaryAssignee = primaryAssignee;
		this.backupAssignee = backupAssignee;
		this.workStatus = workStatus;
		this.priority = priority;
		this.dueDate = dueDate;
		this.criticalKnowledgeRisk = criticalKnowledgeRisk;
		this.needsHandover = needsHandover;
		this.lastReviewedAt = lastReviewedAt;
	}

	public Long getId() {
		return id;
	}

	public String getWorkCode() {
		return workCode;
	}

	public String getTitle() {
		return title;
	}

	public String getSummary() {
		return summary;
	}

	public TeamEntity getOwningTeam() {
		return owningTeam;
	}

	public UserEntity getPrimaryAssignee() {
		return primaryAssignee;
	}

	public UserEntity getBackupAssignee() {
		return backupAssignee;
	}

	public WorkStatus getWorkStatus() {
		return workStatus;
	}

	public WorkPriority getPriority() {
		return priority;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public boolean isCriticalKnowledgeRisk() {
		return criticalKnowledgeRisk;
	}

	public boolean isNeedsHandover() {
		return needsHandover;
	}

	public LocalDateTime getLastReviewedAt() {
		return lastReviewedAt;
	}
}
