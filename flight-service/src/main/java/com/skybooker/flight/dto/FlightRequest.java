package com.skybooker.flight.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FlightRequest(
        @NotBlank String flightNumber,
        @NotNull UUID airlineId,
        @NotBlank String originAirportCode,
        @NotBlank String destinationAirportCode,
        @NotNull @Future LocalDateTime departureTime,
        @NotNull @Future LocalDateTime arrivalTime,
        @NotNull @Min(1) Integer durationMinutes,
        @NotBlank String aircraftType,
        @NotNull @Min(1) Integer totalSeats,
        @NotNull @DecimalMin("0.0") BigDecimal basePrice
) {
}
