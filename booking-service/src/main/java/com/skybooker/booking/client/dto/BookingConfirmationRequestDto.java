package com.skybooker.booking.client.dto;

import java.util.UUID;

public record BookingConfirmationRequestDto(
        UUID recipientId,
        UUID relatedBookingId,
        String recipientEmail,
        String recipientPhone,
        String pnrCode,
        String eticketPdfBase64
) {
}
