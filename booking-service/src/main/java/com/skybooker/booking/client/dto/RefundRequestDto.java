package com.skybooker.booking.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RefundRequestDto(
        UUID paymentId,
        BigDecimal refundAmount
) {
}
