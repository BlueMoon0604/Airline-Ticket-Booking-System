package com.skybooker.auth.dto;

import com.skybooker.auth.entity.Role;

import java.util.UUID;

public record TokenValidationResponse(
        boolean valid,
        UUID userId,
        String email,
        Role role,
        String message
) {
}
