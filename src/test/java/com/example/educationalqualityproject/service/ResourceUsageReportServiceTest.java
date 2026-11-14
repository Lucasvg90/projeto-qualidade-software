package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.EducationalResource;
import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.entity.ReservationStatus;
import com.example.educationalqualityproject.entity.ResourceType;
import com.example.educationalqualityproject.repository.AuditEventRepository;
import com.example.educationalqualityproject.repository.EducationalResourceRepository;
import com.example.educationalqualityproject.repository.ReservationRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResourceUsageReportServiceTest {

    @Test
    void reportsApprovedHoursAndPreventedConflictsPerResource() {
        EducationalResource resource = new EducationalResource();
        resource.setId("room-1");
        resource.setName("Sala 1");
        resource.setType(ResourceType.SALA);

        Reservation approved = new Reservation();
        approved.setResourceIds(List.of("room-1"));
        approved.setStartAt(Instant.parse("2026-12-01T10:00:00Z"));
        approved.setEndAt(Instant.parse("2026-12-01T11:30:00Z"));
        approved.setStatus(ReservationStatus.APPROVED);

        Reservation pending = new Reservation();
        pending.setResourceIds(List.of("room-1"));
        pending.setStartAt(Instant.parse("2026-12-01T12:00:00Z"));
        pending.setEndAt(Instant.parse("2026-12-01T13:00:00Z"));
        pending.setStatus(ReservationStatus.PENDING_APPROVAL);

        EducationalResourceRepository resources = mock(EducationalResourceRepository.class);
        ReservationRepository reservations = mock(ReservationRepository.class);
        AuditLogService auditLog = new AuditLogService(mock(AuditEventRepository.class)) {
            @Override
            public long countPreventedConflicts(String resourceId) {
                return 3;
            }
        };
        when(resources.findAll()).thenReturn(List.of(resource));
        when(reservations.findAll()).thenReturn(List.of(approved, pending));

        var report = new ResourceUsageReportService(resources, reservations, auditLog).getReport();

        assertEquals(1, report.size());
        assertEquals(1, report.get(0).approvedReservations());
        assertEquals("1.50", report.get(0).allocatedHours().toPlainString());
        assertEquals(3, report.get(0).preventedConflicts());
    }
}
