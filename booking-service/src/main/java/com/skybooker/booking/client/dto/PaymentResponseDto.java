package com.skybooker.booking.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponseDto(
        UUID paymentId,
        UUID bookingId,
        UUID userId,
        BigDecimal amount,
        String currency,
        String status,
        String paymentMode,
        String transactionId,
        String gatewayResponse,
        LocalDateTime paidAt,
        LocalDateTime refundedAt,
        BigDecimal refundAmount,
        String paymentSessionUrl
) {
}
