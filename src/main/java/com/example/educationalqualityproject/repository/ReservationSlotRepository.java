package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.ReservationSlot;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservationSlotRepository extends MongoRepository<ReservationSlot, String> {
    void deleteByReservationId(String reservationId);
}
