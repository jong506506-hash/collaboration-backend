package com.juteak.collaboration.persistence.entity;

import java.time.LocalDate;

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
 * Reusable knowledge that should be searchable even when the original owner is away.
 */
@Entity
@Table(name = "collab_knowledge_article")
public class KnowledgeArticleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owning_team_id", nullable = false)
	private TeamEntity owningTeam;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private UserEntity author;

	@Column(name = "title", nullable = false)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false, length = 32)
	private KnowledgeCategory category;

	@Lob
	@Column(name = "body_summary")
	private String bodySummary;

	@Column(name = "keywords", length = 500)
	private String keywords;

	@Column(name = "related_work_code", length = 64)
	private String relatedWorkCode;

	@Column(name = "stale_after_date")
	private LocalDate staleAfterDate;

	protected KnowledgeArticleEntity() {
	}
}
