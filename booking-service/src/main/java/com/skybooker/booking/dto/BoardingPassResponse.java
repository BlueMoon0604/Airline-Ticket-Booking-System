package com.skybooker.booking.dto;

import java.util.UUID;

public record BoardingPassResponse(
        UUID bookingId,
        UUID passengerId,
        String fileName,
        String contentType,
        String content
) {
}
