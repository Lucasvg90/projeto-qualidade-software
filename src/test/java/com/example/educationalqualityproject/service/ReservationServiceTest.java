package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.dto.ReservationRequest;
import com.example.educationalqualityproject.entity.EducationalResource;
import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.entity.ReservationSlot;
import com.example.educationalqualityproject.entity.ReservationStatus;
import com.example.educationalqualityproject.entity.ResourceType;
import com.example.educationalqualityproject.repository.EducationalResourceRepository;
import com.example.educationalqualityproject.repository.ReservationRepository;
import com.example.educationalqualityproject.repository.ReservationSlotRepository;
import com.example.educationalqualityproject.repository.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationSlotRepository slotRepository;
    @Mock
    private EducationalResourceRepository resourceRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private AuditRecorder auditRecorder;
    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void restrictedResourceRequiresApprovalAndLocksEveryMinuteForTeacherAndResource() {
        EducationalResource room = new EducationalResource();
        room.setId("room-1");
        room.setType(ResourceType.SALA);
        room.setRestricted(true);
        when(resourceRepository.existsById("room-1")).thenReturn(true);
        when(resourceRepository.findAllById(anyIterable())).thenReturn(List.of(room));
        when(teacherRepository.existsById("teacher-1")).thenReturn(true);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setId("reservation-1");
            return reservation;
        });

        Instant start = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MINUTES);
        Reservation result = reservationService.create(new ReservationRequest(
                List.of("room-1"), "teacher-1", start, start.plus(2, ChronoUnit.MINUTES)), "requester@example.com");

        assertEquals(ReservationStatus.PENDING_APPROVAL, result.getStatus());
        ArgumentCaptor<Iterable<ReservationSlot>> slotsCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(slotRepository).saveAll(slotsCaptor.capture());
        List<ReservationSlot> slots = StreamSupport.stream(slotsCaptor.getValue().spliterator(), false).toList();
        assertEquals(4, slots.size());
        assertEquals(List.of("resource:room-1", "resource:room-1", "teacher:teacher-1", "teacher:teacher-1"),
                slots.stream().map(ReservationSlot::getResourceKey).sorted().toList());
        assertEquals(List.of("reservation-1"), slots.stream().map(ReservationSlot::getReservationId).distinct().toList());
        verify(auditRecorder).record("RESERVATION", "reservation-1", "RESERVATION_CREATED",
                "requester@example.com", "Depois: recursos=[room-1], professor=teacher-1, início="
                        + start + ", fim=" + start.plus(2, ChronoUnit.MINUTES)
                        + ", status=PENDING_APPROVAL");
        verify(notificationSender).send("requester@example.com", "RESERVATION_CREATED", "reservation-1",
                "Sua reserva foi criada com status PENDING_APPROVAL.");
    }

    @Test
    void requesterCanLoadTheirEditableReservationButNotAnotherRequesters() {
        Reservation reservation = new Reservation();
        reservation.setId("reservation-1");
        reservation.setRequestedBy("owner@example.com");
        reservation.setStatus(ReservationStatus.APPROVED);
        when(reservationRepository.findById("reservation-1")).thenReturn(Optional.of(reservation));

        assertSame(reservation, reservationService.getForRequester("reservation-1", "owner@example.com"));
        assertThrows(AccessDeniedException.class,
                () -> reservationService.getForRequester("reservation-1", "other@example.com"));
    }

    @Test
    void resourceUnderMaintenanceCannotBeReserved() {
        EducationalResource resource = new EducationalResource();
        resource.setId("material-1");
        resource.setName("Projetor");
        resource.setType(ResourceType.EQUIPAMENTO);
        resource.setUnderMaintenance(true);
        when(resourceRepository.existsById("material-1")).thenReturn(true);
        when(resourceRepository.findAllById(anyIterable())).thenReturn(List.of(resource));

        Instant start = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MINUTES);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reservationService.create(new ReservationRequest(
                List.of("material-1"), null, start, start.plus(1, ChronoUnit.HOURS)), "requester@example.com"));
        assertTrue(exception.getReason().contains("está em manutenção"));
    }
}
