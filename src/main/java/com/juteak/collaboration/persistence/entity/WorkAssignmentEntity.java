package com.juteak.collaboration.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Separate assignment records let one work item have multiple collaborators without overloading the main table.
 */
@Entity
@Table(name = "collab_work_assignment")
public class WorkAssignmentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "work_item_id", nullable = false)
	private WorkItemEntity workItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_id", nullable = false)
	private UserEntity assignee;

	@Enumerated(EnumType.STRING)
	@Column(name = "assignment_type", nullable = false, length = 32)
	private AssignmentType assignmentType;

	@Column(name = "note", length = 500)
	private String note;

	protected WorkAssignmentEntity() {
	}
}
