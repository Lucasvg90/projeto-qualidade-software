package com.example.educationalqualityproject.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "reservation_slots")
public class ReservationSlot {

    @Id
    private String id;
    private String resourceKey;
    private Instant slotStart;
    private String reservationId;

    public ReservationSlot() {
    }

    public ReservationSlot(String resourceKey, Instant slotStart, String reservationId) {
        this.resourceKey = resourceKey;
        this.slotStart = slotStart;
        this.reservationId = reservationId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public Instant getSlotStart() {
        return slotStart;
    }

    public void setSlotStart(Instant slotStart) {
        this.slotStart = slotStart;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }
}
