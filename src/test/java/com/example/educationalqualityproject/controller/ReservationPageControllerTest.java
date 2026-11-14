package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.dto.ReservationForm;
import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.repository.EducationalResourceRepository;
import com.example.educationalqualityproject.repository.TeacherRepository;
import com.example.educationalqualityproject.service.AuditRecorder;
import com.example.educationalqualityproject.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.ui.ExtendedModelMap;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationPageControllerTest {

    @Test
    void editLoadsReservationIntoTheFormForItsRequester() {
        CapturingReservationService reservationService = new CapturingReservationService();
        EducationalResourceRepository resourceRepository = mock(EducationalResourceRepository.class);
        TeacherRepository teacherRepository = mock(TeacherRepository.class);
        AuditRecorder auditRecorder = mock(AuditRecorder.class);
        ReservationPageController controller = new ReservationPageController(
                reservationService, resourceRepository, teacherRepository, auditRecorder);

        Instant start = Instant.parse("2026-12-01T10:00:00Z");
        Reservation reservation = new Reservation();
        reservation.setId("reservation-1");
        reservation.setResourceIds(List.of("room-1"));
        reservation.setTeacherId("teacher-1");
        reservation.setStartAt(start);
        reservation.setEndAt(start.plusSeconds(3600));
        reservationService.reservation = reservation;
        when(resourceRepository.findAll()).thenReturn(List.of());
        when(teacherRepository.findAll()).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.edit(
                "reservation-1",
                new UsernamePasswordAuthenticationToken("owner@example.com", "n/a"),
                model);

        assertEquals("reservation/list", view);
        assertEquals("reservation-1", model.get("reservationId"));
        ReservationForm form = (ReservationForm) model.get("reservationForm");
        assertEquals(List.of("room-1"), form.getResourceIds());
        assertEquals("teacher-1", form.getTeacherId());
        ZoneId zone = ZoneId.systemDefault();
        assertEquals(LocalDateTime.ofInstant(start, zone), form.getStartAt());
        assertEquals(LocalDateTime.ofInstant(start.plusSeconds(3600), zone), form.getEndAt());
        assertEquals("reservation-1", reservationService.requestedId);
        assertEquals("owner@example.com", reservationService.requester);
    }

    private static class CapturingReservationService extends ReservationService {

        private Reservation reservation;
        private String requestedId;
        private String requester;

        private CapturingReservationService() {
            super(null, null, null, null, null, null);
        }

        @Override
        public Reservation getForRequester(String id, String requester) {
            this.requestedId = id;
            this.requester = requester;
            return reservation;
        }

        @Override
        public List<Reservation> getForRequester(String requester) {
            return List.of(reservation);
        }
    }
}
