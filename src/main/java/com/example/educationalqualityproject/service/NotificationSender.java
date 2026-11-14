package com.example.educationalqualityproject.service;

public interface NotificationSender {
    void send(String recipient, String eventType, String entityId, String message);
}
