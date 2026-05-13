package com.skybooker.auth.dto;

public record UpdateProfileRequest(
        String fullName,
        String phone,
        String passportNumber,
        String nationality
) {
}
