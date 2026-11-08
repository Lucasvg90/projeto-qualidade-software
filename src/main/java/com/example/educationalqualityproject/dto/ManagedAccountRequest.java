package com.example.educationalqualityproject.dto;

import com.example.educationalqualityproject.entity.UserRole;

public record ManagedAccountRequest(String name, String email, String password, UserRole role) {
}
