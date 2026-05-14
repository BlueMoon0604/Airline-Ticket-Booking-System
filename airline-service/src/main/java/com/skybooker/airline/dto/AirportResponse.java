package com.skybooker.airline.dto;

import java.util.UUID;

public record AirportResponse(
        UUID airportId,
        String name,
        String iataCode,
        String icaoCode,
        String city,
        String country,
        Double latitude,
        Double longitude,
        String timezone
) {
}
