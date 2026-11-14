package com.example.educationalqualityproject.dto;

import jakarta.validation.constraints.NotBlank;

public record MaterialCheckoutRequest(
        @NotBlank String reservationId,
        @NotBlank String resourceId) {
}
