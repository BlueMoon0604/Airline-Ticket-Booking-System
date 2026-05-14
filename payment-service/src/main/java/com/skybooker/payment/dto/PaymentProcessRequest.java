package com.skybooker.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentProcessRequest(
        @NotBlank String gatewayTransactionId,
        @NotBlank String gatewayResponse
) {
}
