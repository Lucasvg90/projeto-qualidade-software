package com.example.educationalqualityproject.dto;

import com.example.educationalqualityproject.entity.UserAccount;
import com.example.educationalqualityproject.entity.UserRole;

import java.time.Instant;

public record AccountResponse(String id, String name, String email, UserRole role, Instant createdAt) {

    public static AccountResponse from(UserAccount account) {
        return new AccountResponse(
                account.getId(), account.getName(), account.getEmail(), account.getRole(), account.getCreatedAt());
    }
}
