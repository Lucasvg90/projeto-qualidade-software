package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.dto.ReservationRequest;
import com.example.educationalqualityproject.entity.EducationalResource;
import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.entity.ResourceType;
import com.example.educationalqualityproject.repository.EducationalResourceRepository;
import com.example.educationalqualityproject.repository.ReservationRepository;
import com.example.educationalqualityproject.repository.ReservationSlotRepository;
import com.example.educationalqualityproject.repository.TeacherRepository;
import com.example.educationalqualityproject.service.AuditRecorder;
import com.example.educationalqualityproject.service.NotificationSender;
import com.example.educationalqualityproject.service.ReservationService;
import com.mongodb.MongoException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationApiControllerTest {

    @Test
    void recordsTransientTransactionConflictAndReturnsConflictException() {
        MongoException transientError = new MongoException(112, "Write conflict");
        transientError.addLabel("TransientTransactionError");
        var fixture = controllerWithSaveFailure(
                new UncategorizedMongoDbException("Transaction failed", transientError));

        assertThrows(DuplicateKeyException.class,
                () -> fixture.controller.create(request(), requester()));
        verify(fixture.auditRecorder).recordPreventedConflicts(List.of("room-1"), "requester@example.com");
    }

    @Test
    void doesNotHideNonTransientMongoErrors() {
        var failure = new UncategorizedMongoDbException("MongoDB unavailable", new MongoException("connection failed"));
        var fixture = controllerWithSaveFailure(failure);

        assertThrows(UncategorizedMongoDbException.class,
                () -> fixture.controller.create(request(), requester()));
        verify(fixture.auditRecorder, org.mockito.Mockito.never()).recordPreventedConflicts(any(), any());
    }

    private Fixture controllerWithSaveFailure(RuntimeException failure) {
        ReservationRepository reservations = mock(ReservationRepository.class);
        ReservationSlotRepository slots = mock(ReservationSlotRepository.class);
        EducationalResourceRepository resources = mock(EducationalResourceRepository.class);
        TeacherRepository teachers = mock(TeacherRepository.class);
        AuditRecorder auditRecorder = mock(AuditRecorder.class);
        NotificationSender notifications = mock(NotificationSender.class);
        EducationalResource room = new EducationalResource();
        room.setId("room-1");
        room.setType(ResourceType.SALA);
        room.setCapacity(20);

        when(resources.existsById("room-1")).thenReturn(true);
        when(resources.findAllById(any())).thenReturn(List.of(room));
        when(reservations.save(any(Reservation.class))).thenThrow(failure);

        ReservationService reservationService = new ReservationService(
                reservations, slots, resources, teachers, auditRecorder, notifications);
        return new Fixture(new ReservationApiController(reservationService, auditRecorder), auditRecorder);
    }

    private ReservationRequest request() {
        return new ReservationRequest(
                List.of("room-1"),
                null,
                Instant.parse("2026-12-01T10:00:00Z"),
                Instant.parse("2026-12-01T11:00:00Z"));
    }

    private UsernamePasswordAuthenticationToken requester() {
        return new UsernamePasswordAuthenticationToken(
                "requester@example.com", "ignored", List.of(new SimpleGrantedAuthority("ROLE_SOLICITANTE")));
    }

    private record Fixture(ReservationApiController controller, AuditRecorder auditRecorder) {
    }
}
