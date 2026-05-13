package com.skybooker.booking.client.dto;

import java.util.UUID;

public record SeatAssignmentRequestDto(
        UUID seatId,
        String seatNumber
) {
}
