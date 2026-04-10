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
 * Handover content focuses on continuity: current state, pending actions, system links, and known risks.
 */
@Entity
@Table(name = "collab_handover_document")
public class HandoverDocumentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "work_item_id", nullable = false)
	private WorkItemEntity workItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private UserEntity author;

	@Column(name = "title", nullable = false)
	private String title;

	@Lob
	@Column(name = "current_state_summary")
	private String currentStateSummary;

	@Lob
	@Column(name = "pending_actions")
	private String pendingActions;

	@Column(name = "related_systems", length = 500)
	private String relatedSystems;

	@Column(name = "key_contacts", length = 500)
	private String keyContacts;

	@Lob
	@Column(name = "handover_notes")
	private String handoverNotes;

	@Lob
	@Column(name = "risk_notes")
	private String riskNotes;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "last_verified_at")
	private LocalDateTime lastVerifiedAt;

	protected HandoverDocumentEntity() {
	}

	public HandoverDocumentEntity(
		WorkItemEntity workItem,
		UserEntity author,
		String title,
		String currentStateSummary,
		String pendingActions,
		String relatedSystems,
		String keyContacts,
		String handoverNotes,
		String riskNotes,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		LocalDateTime lastVerifiedAt
	) {
		this.workItem = workItem;
		this.author = author;
		this.title = title;
		this.currentStateSummary = currentStateSummary;
		this.pendingActions = pendingActions;
		this.relatedSystems = relatedSystems;
		this.keyContacts = keyContacts;
		this.handoverNotes = handoverNotes;
		this.riskNotes = riskNotes;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.lastVerifiedAt = lastVerifiedAt;
	}

	public void update(
		UserEntity author,
		String title,
		String currentStateSummary,
		String pendingActions,
		String relatedSystems,
		String keyContacts,
		String handoverNotes,
		String riskNotes,
		LocalDateTime updatedAt,
		LocalDateTime lastVerifiedAt
	) {
		this.author = author;
		this.title = title;
		this.currentStateSummary = currentStateSummary;
		this.pendingActions = pendingActions;
		this.relatedSystems = relatedSystems;
		this.keyContacts = keyContacts;
		this.handoverNotes = handoverNotes;
		this.riskNotes = riskNotes;
		this.updatedAt = updatedAt;
		this.lastVerifiedAt = lastVerifiedAt;
	}

	public Long getId() {
		return id;
	}

	public WorkItemEntity getWorkItem() {
		return workItem;
	}

	public UserEntity getAuthor() {
		return author;
	}

	public String getTitle() {
		return title;
	}

	public String getCurrentStateSummary() {
		return currentStateSummary;
	}

	public String getPendingActions() {
		return pendingActions;
	}

	public String getRelatedSystems() {
		return relatedSystems;
	}

	public String getKeyContacts() {
		return keyContacts;
	}

	public String getHandoverNotes() {
		return handoverNotes;
	}

	public String getRiskNotes() {
		return riskNotes;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public LocalDateTime getLastVerifiedAt() {
		return lastVerifiedAt;
	}
}
