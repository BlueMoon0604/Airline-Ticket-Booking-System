package com.skybooker.payment.dto;

import com.skybooker.payment.entity.PaymentMode;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        @NotNull UUID bookingId,
        @NotNull UUID userId,
        @NotNull BigDecimal amount,
        @NotNull PaymentMode paymentMode
) {
}
