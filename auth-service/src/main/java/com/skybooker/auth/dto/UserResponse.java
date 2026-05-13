package com.skybooker.auth.dto;

import com.skybooker.auth.entity.AuthProvider;
import com.skybooker.auth.entity.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        String fullName,
        String email,
        String phone,
        Role role,
        AuthProvider provider,
        Boolean isActive,
        String passportNumber,
        String nationality,
        LocalDateTime createdAt
) {
}
