package com.juteak.collaboration.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juteak.collaboration.persistence.entity.QuestionThreadEntity;

/**
 * Repository entry point for routed question threads.
 */
public interface QuestionThreadRepository extends JpaRepository<QuestionThreadEntity, Long> {
}
