package com.skybooker.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record RefundRequest(
        @NotNull UUID paymentId,
        @NotNull @DecimalMin("0.0") BigDecimal refundAmount
) {
}
