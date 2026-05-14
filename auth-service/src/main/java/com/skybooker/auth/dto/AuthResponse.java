package com.skybooker.auth.dto;

import com.skybooker.auth.entity.Role;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String fullName,
        String email,
        Role role,
        String accessToken,
        String refreshToken
) {
}
