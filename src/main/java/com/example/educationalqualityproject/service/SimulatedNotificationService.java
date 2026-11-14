package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.NotificationEvent;
import com.example.educationalqualityproject.repository.NotificationEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SimulatedNotificationService implements NotificationSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedNotificationService.class);

    private final NotificationEventRepository repository;

    public SimulatedNotificationService(NotificationEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public void send(String recipient, String eventType, String entityId, String message) {
        NotificationEvent notification = repository.save(
                new NotificationEvent(recipient, eventType, entityId, message));
        LOGGER.info("Notificação simulada registrada: eventType={}, entityId={}, notificationId={}",
                eventType, entityId, notification.getId());
    }
}
