package com.skybooker.flight.dto;

import com.skybooker.flight.entity.FlightStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FlightResponse(
        UUID flightId,
        String flightNumber,
        UUID airlineId,
        String originAirportCode,
        String destinationAirportCode,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        Integer durationMinutes,
        FlightStatus status,
        String aircraftType,
        Integer totalSeats,
        Integer availableSeats,
        BigDecimal basePrice
) {
}
