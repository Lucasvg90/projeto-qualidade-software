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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    }
}
