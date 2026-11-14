package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.AuditEvent;
import com.example.educationalqualityproject.repository.AuditEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService implements AuditRecorder {

    private static final String RESOURCE = "RESOURCE";
    private static final String PREVENTED_CONFLICT = "RESERVATION_CONFLICT_PREVENTED";

    private final AuditEventRepository repository;

    public AuditLogService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public void record(String entityType, String entityId, String action, String actor, String details) {
        repository.save(new AuditEvent(entityType, entityId, action, actor, details));
    }

    @Override
    public void recordPreventedConflicts(List<String> resourceIds, String actor) {
        resourceIds.stream().distinct().forEach(resourceId ->
                record(RESOURCE, resourceId, PREVENTED_CONFLICT, actor,
                        "Solicitação concorrente bloqueada pelo índice de disponibilidade."));
    }

    public List<AuditEvent> list(String entityType, String entityId) {
        if (entityType != null && entityId != null) {
            return repository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(entityType, entityId);
        }
        return repository.findAllByOrderByOccurredAtDesc().stream()
                .filter(event -> entityType == null || entityType.equals(event.getEntityType()))
                .filter(event -> entityId == null || entityId.equals(event.getEntityId()))
                .toList();
    }

    public long countPreventedConflicts(String resourceId) {
        return repository.countByActionAndEntityTypeAndEntityId(PREVENTED_CONFLICT, RESOURCE, resourceId);
    }
}
