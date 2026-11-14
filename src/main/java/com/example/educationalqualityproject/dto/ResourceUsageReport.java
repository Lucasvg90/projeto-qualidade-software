package com.example.educationalqualityproject.dto;

import com.example.educationalqualityproject.entity.ResourceType;

import java.math.BigDecimal;

public record ResourceUsageReport(
        String resourceId,
        String resourceName,
        ResourceType type,
        long approvedReservations,
        BigDecimal allocatedHours,
        long preventedConflicts) {
}
