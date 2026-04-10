package com.juteak.collaboration.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juteak.collaboration.persistence.entity.QuestionAnswerEntity;

/**
 * Repository for answer history attached to one question thread.
 */
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswerEntity, Long> {

	List<QuestionAnswerEntity> findByQuestionThreadIdOrderByCreatedAtAsc(Long questionThreadId);
}
