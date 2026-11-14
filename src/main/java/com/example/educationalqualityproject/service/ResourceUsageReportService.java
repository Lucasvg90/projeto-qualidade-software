package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.dto.ResourceUsageReport;
import com.example.educationalqualityproject.entity.EducationalResource;
import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.entity.ReservationStatus;
import com.example.educationalqualityproject.repository.EducationalResourceRepository;
import com.example.educationalqualityproject.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResourceUsageReportService {

    private final EducationalResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final AuditLogService auditLogService;

    public ResourceUsageReportService(
            EducationalResourceRepository resourceRepository,
            ReservationRepository reservationRepository,
            AuditLogService auditLogService) {
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
        this.auditLogService = auditLogService;
    }

    public List<ResourceUsageReport> getReport() {
        Map<String, Usage> usageByResource = new HashMap<>();
        for (Reservation reservation : reservationRepository.findAll()) {
            if (reservation.getStatus() != ReservationStatus.APPROVED) {
                continue;
            }
            long allocatedMillis = Duration.between(
                    reservation.getStartAt(), reservation.getEndAt()).toMillis();
            for (String resourceId : reservation.getResourceIds()) {
                Usage usage = usageByResource.computeIfAbsent(resourceId, ignored -> new Usage());
                usage.reservations++;
                usage.allocatedMillis += allocatedMillis;
            }
        }

        return resourceRepository.findAll().stream()
                .map(resource -> toReport(resource, usageByResource.getOrDefault(resource.getId(), new Usage())))
                .toList();
    }

    private ResourceUsageReport toReport(EducationalResource resource, Usage usage) {
        return new ResourceUsageReport(
                resource.getId(),
                resource.getName(),
                resource.getType(),
                usage.reservations,
                BigDecimal.valueOf(usage.allocatedMillis)
                        .divide(BigDecimal.valueOf(3_600_000), 2, RoundingMode.HALF_UP),
                auditLogService.countPreventedConflicts(resource.getId()));
    }

    private static class Usage {
        private long reservations;
        private long allocatedMillis;
    }
}
