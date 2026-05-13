package com.skybooker.airline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AirportRequest(
        @NotBlank String name,
        @NotBlank String iataCode,
        String icaoCode,
        @NotBlank String city,
        @NotBlank String country,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotBlank String timezone
) {
}
