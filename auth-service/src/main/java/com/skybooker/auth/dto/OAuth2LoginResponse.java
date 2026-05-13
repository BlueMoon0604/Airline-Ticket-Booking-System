package com.skybooker.auth.dto;

public record OAuth2LoginResponse(
        String message,
        String redirectUrl,
        AuthResponse auth
) {
}
