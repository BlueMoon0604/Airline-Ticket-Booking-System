package com.skybooker.flight.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RoundTripSearchRequest(
        @NotBlank String originAirportCode,
        @NotBlank String destinationAirportCode,
        @NotNull LocalDate departureDate,
        @NotNull LocalDate returnDate,
        @Min(1) Integer passengers
) {
}
