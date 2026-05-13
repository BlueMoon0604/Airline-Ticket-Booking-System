package com.skybooker.payment.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponseDto(
        UUID bookingId,
        UUID userId,
        UUID flightId,
        String pnrCode,
        String tripType,
        String status,
        BigDecimal totalFare,
        BigDecimal baseFare,
        BigDecimal taxes,
        String mealPreference,
        Integer luggageKg,
        String contactEmail,
        String contactPhone,
        LocalDateTime bookedAt,
        UUID paymentId,
        String paymentSessionUrl
) {
}
