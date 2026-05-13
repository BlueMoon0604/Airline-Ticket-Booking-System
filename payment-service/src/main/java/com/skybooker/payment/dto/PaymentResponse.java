package com.skybooker.payment.dto;

import com.skybooker.payment.entity.PaymentMode;
import com.skybooker.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID bookingId,
        UUID userId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        PaymentMode paymentMode,
        String transactionId,
        String gatewayOrderId,
        String gatewayName,
        String gatewaySignature,
        String gatewayResponse,
        LocalDateTime paidAt,
        LocalDateTime refundedAt,
        BigDecimal refundAmount,
        String paymentSessionUrl,
        String razorpayKeyId
) {
}
