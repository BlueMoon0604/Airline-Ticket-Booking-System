package com.skybooker.airline.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AirlineRequest(
        @NotBlank String name,
        @NotBlank String iataCode,
        String icaoCode,
        String logoUrl,
        @NotBlank String country,
        @Email String contactEmail,
        String contactPhone
) {
}
