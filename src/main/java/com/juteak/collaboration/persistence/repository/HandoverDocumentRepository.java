package com.juteak.collaboration.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.juteak.collaboration.persistence.entity.HandoverDocumentEntity;
import com.juteak.collaboration.persistence.entity.WorkItemEntity;

public interface HandoverDocumentRepository extends JpaRepository<HandoverDocumentEntity, Long> {

	List<HandoverDocumentEntity> findAllByOrderByUpdatedAtDesc();

	List<HandoverDocumentEntity> findByWorkItemOrderByUpdatedAtDesc(WorkItemEntity workItem);
}
