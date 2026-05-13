package com.skybooker.booking.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentInitiateRequestDto(
        UUID bookingId,
        UUID userId,
        BigDecimal amount,
        String paymentMode
) {
}
