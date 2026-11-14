package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.MaterialCheckout;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialCheckoutRepository extends MongoRepository<MaterialCheckout, String> {
    Optional<MaterialCheckout> findByReservationIdAndResourceId(String reservationId, String resourceId);
    List<MaterialCheckout> findAllByOrderByWithdrawnAtDesc();
}
