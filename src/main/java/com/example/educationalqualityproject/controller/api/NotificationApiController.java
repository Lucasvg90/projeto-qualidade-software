package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.entity.NotificationEvent;
import com.example.educationalqualityproject.repository.NotificationEventRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final NotificationEventRepository repository;

    public NotificationApiController(NotificationEventRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<NotificationEvent> list(Authentication authentication) {
        return repository.findByRecipientOrderByCreatedAtDesc(authentication.getName());
    }
}
