package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.AuditEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
    List<AuditEvent> findAllByOrderByOccurredAtDesc();
    List<AuditEvent> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(String entityType, String entityId);
    long countByActionAndEntityTypeAndEntityId(String action, String entityType, String entityId);
}
