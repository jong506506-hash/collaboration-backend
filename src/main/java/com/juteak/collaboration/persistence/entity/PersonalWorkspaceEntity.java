package com.juteak.collaboration.persistence.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Stores a person's durable working context so questions do not depend on one person's memory.
 */
@Entity
@Table(name = "collab_personal_workspace")
public class PersonalWorkspaceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_user_id", nullable = false, unique = true)
	private UserEntity owner;

	@Column(name = "title", nullable = false)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(name = "visibility_scope", nullable = false, length = 32)
	private WorkspaceVisibility visibilityScope;

	@Lob
	@Column(name = "role_summary")
	private String roleSummary;

	@Lob
	@Column(name = "responsibility_summary")
	private String responsibilitySummary;

	@Lob
	@Column(name = "current_focus")
	private String currentFocus;

	@Lob
	@Column(name = "recurring_work_notes")
	private String recurringWorkNotes;

	@Lob
	@Column(name = "faq_notes")
	private String faqNotes;

	@Lob
	@Column(name = "caution_notes")
	private String cautionNotes;

	@Lob
	@Column(name = "system_notes")
	private String systemNotes;

	@Lob
	@Column(name = "reference_links")
	private String referenceLinks;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@ManyToMany
	@JoinTable(
		name = "collab_workspace_work_link",
		joinColumns = @JoinColumn(name = "workspace_id"),
		inverseJoinColumns = @JoinColumn(name = "work_item_id")
	)
	private Set<WorkItemEntity> linkedWorkItems = new LinkedHashSet<>();

	@ManyToMany
	@JoinTable(
		name = "collab_workspace_handover_link",
		joinColumns = @JoinColumn(name = "workspace_id"),
		inverseJoinColumns = @JoinColumn(name = "handover_document_id")
	)
	private Set<HandoverDocumentEntity> linkedHandoverDocuments = new LinkedHashSet<>();

	protected PersonalWorkspaceEntity() {
	}

	public PersonalWorkspaceEntity(
		UserEntity owner,
		String title,
		WorkspaceVisibility visibilityScope,
		String roleSummary,
		String responsibilitySummary,
		String currentFocus,
		String recurringWorkNotes,
		String faqNotes,
		String cautionNotes,
		String systemNotes,
		String referenceLinks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
		this.owner = owner;
		this.title = title;
		this.visibilityScope = visibilityScope;
		this.roleSummary = roleSummary;
		this.responsibilitySummary = responsibilitySummary;
		this.currentFocus = currentFocus;
		this.recurringWorkNotes = recurringWorkNotes;
		this.faqNotes = faqNotes;
		this.cautionNotes = cautionNotes;
		this.systemNotes = systemNotes;
		this.referenceLinks = referenceLinks;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public void update(
		String title,
		WorkspaceVisibility visibilityScope,
		String roleSummary,
		String responsibilitySummary,
		String currentFocus,
		String recurringWorkNotes,
		String faqNotes,
		String cautionNotes,
		String systemNotes,
		String referenceLinks,
		LocalDateTime updatedAt
	) {
		this.title = title;
		this.visibilityScope = visibilityScope;
		this.roleSummary = roleSummary;
		this.responsibilitySummary = responsibilitySummary;
		this.currentFocus = currentFocus;
		this.recurringWorkNotes = recurringWorkNotes;
		this.faqNotes = faqNotes;
		this.cautionNotes = cautionNotes;
		this.systemNotes = systemNotes;
		this.referenceLinks = referenceLinks;
		this.updatedAt = updatedAt;
	}

	public void appendFaqNotes(String additionalFaqNotes, LocalDateTime updatedAt) {
		if (additionalFaqNotes == null || additionalFaqNotes.isBlank()) {
			return;
		}

		if (this.faqNotes == null || this.faqNotes.isBlank()) {
			this.faqNotes = additionalFaqNotes;
		} else {
			this.faqNotes = this.faqNotes.stripTrailing() + "\n\n---\n" + additionalFaqNotes;
		}
		this.updatedAt = updatedAt;
	}

	public void replaceLinkedWorkItems(Set<WorkItemEntity> workItems) {
		this.linkedWorkItems.clear();
		this.linkedWorkItems.addAll(workItems);
	}

	public void replaceLinkedHandoverDocuments(Set<HandoverDocumentEntity> handoverDocuments) {
		this.linkedHandoverDocuments.clear();
		this.linkedHandoverDocuments.addAll(handoverDocuments);
	}

	public Long getId() {
		return id;
	}

	public UserEntity getOwner() {
		return owner;
	}

	public String getTitle() {
		return title;
	}

	public WorkspaceVisibility getVisibilityScope() {
		return visibilityScope;
	}

	public String getRoleSummary() {
		return roleSummary;
	}

	public String getResponsibilitySummary() {
		return responsibilitySummary;
	}

	public String getCurrentFocus() {
		return currentFocus;
	}

	public String getRecurringWorkNotes() {
		return recurringWorkNotes;
	}

	public String getFaqNotes() {
		return faqNotes;
	}

	public String getCautionNotes() {
		return cautionNotes;
	}

	public String getSystemNotes() {
		return systemNotes;
	}

	public String getReferenceLinks() {
		return referenceLinks;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public Set<WorkItemEntity> getLinkedWorkItems() {
		return linkedWorkItems;
	}

	public Set<HandoverDocumentEntity> getLinkedHandoverDocuments() {
		return linkedHandoverDocuments;
	}
}
