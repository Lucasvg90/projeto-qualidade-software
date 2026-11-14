package com.example.educationalqualityproject.service;

import java.util.List;

public interface AuditRecorder {
    void record(String entityType, String entityId, String action, String actor, String details);
    void recordPreventedConflicts(List<String> resourceIds, String actor);
}
