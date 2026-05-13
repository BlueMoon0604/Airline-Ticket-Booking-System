package com.skybooker.airline.dto;

import java.util.UUID;

public record AirlineResponse(
        UUID airlineId,
        String name,
        String iataCode,
        String icaoCode,
        String logoUrl,
        String country,
        String contactEmail,
        String contactPhone,
        Boolean isActive
) {
}
