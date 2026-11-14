package com.example.educationalqualityproject.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notification_events")
public class NotificationEvent {

    @Id
    private String id;
    private String channel = "SIMULATED";
    private String recipient;
    private String eventType;
    private String entityId;
    private String message;
    private Instant createdAt;

    public NotificationEvent() {
        this.createdAt = Instant.now();
    }

    public NotificationEvent(String recipient, String eventType, String entityId, String message) {
        this();
        this.recipient = recipient;
        this.eventType = eventType;
        this.entityId = entityId;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
