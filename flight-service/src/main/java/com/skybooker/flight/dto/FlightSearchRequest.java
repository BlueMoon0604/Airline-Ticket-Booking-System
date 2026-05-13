package com.skybooker.flight.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FlightSearchRequest(
        @NotBlank String originAirportCode,
        @NotBlank String destinationAirportCode,
        @NotNull LocalDate departureDate,
        @Min(1) Integer passengers
) {
}
