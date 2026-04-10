package com.juteak.collaboration.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juteak.collaboration.persistence.entity.KnowledgeArticleEntity;

/**
 * Repository entry point for reusable knowledge records.
 */
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticleEntity, Long> {
}
