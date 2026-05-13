package com.skybooker.auth.dto;

import com.skybooker.auth.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Password must be at least 8 chars with upper, lower, and number"
        )
        String password,
        @NotBlank String phone,
        String passportNumber,
        String nationality,
        Role role
) {
}
