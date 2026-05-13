package com.skybooker.booking.dto;

import java.util.UUID;

public record TicketResponse(
        UUID bookingId,
        String fileName,
        String contentType,
        String content
) {
}
