package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.entity.AuditEvent;
import com.example.educationalqualityproject.entity.NotificationEvent;
import com.example.educationalqualityproject.repository.NotificationEventRepository;
import com.example.educationalqualityproject.service.AuditLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminAuditApiController {

    private final AuditLogService auditLogService;
    private final NotificationEventRepository notificationRepository;

    public AdminAuditApiController(
            AuditLogService auditLogService,
            NotificationEventRepository notificationRepository) {
        this.auditLogService = auditLogService;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/audit")
    public List<AuditEvent> audit(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId) {
        return auditLogService.list(entityType, entityId);
    }

    @GetMapping("/notifications")
    public List<NotificationEvent> notifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }
}
