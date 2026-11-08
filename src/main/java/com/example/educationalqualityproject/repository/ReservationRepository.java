package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.entity.ReservationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends MongoRepository<Reservation, String> {
    List<Reservation> findByStatusInAndStartAtLessThanAndEndAtGreaterThan(
            Collection<ReservationStatus> statuses, Instant endAt, Instant startAt);
}
