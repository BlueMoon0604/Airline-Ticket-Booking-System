package com.skybooker.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RazorpayOrderResponse(
        UUID paymentId,
        UUID bookingId,
        BigDecimal amount,
        String currency,
        String razorpayOrderId,
        String razorpayKeyId,
        String status
) {
}
