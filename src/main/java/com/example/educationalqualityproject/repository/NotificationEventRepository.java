package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.NotificationEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationEventRepository extends MongoRepository<NotificationEvent, String> {
    List<NotificationEvent> findAllByOrderByCreatedAtDesc();
    List<NotificationEvent> findByRecipientOrderByCreatedAtDesc(String recipient);
}
