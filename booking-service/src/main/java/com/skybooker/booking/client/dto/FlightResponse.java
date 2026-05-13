package com.skybooker.booking.client.dto;

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
        String status,
        String aircraftType,
        Integer totalSeats,
        Integer availableSeats,
        BigDecimal basePrice
) {
}
