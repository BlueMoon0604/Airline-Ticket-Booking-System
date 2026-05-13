package com.skybooker.booking.dto;

import com.skybooker.booking.entity.BookingStatus;
import com.skybooker.booking.entity.TripType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID bookingId,
        UUID userId,
        UUID flightId,
        String pnrCode,
        TripType tripType,
        BookingStatus status,
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
