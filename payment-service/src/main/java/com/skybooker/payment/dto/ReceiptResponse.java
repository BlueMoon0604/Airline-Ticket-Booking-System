package com.skybooker.payment.dto;

import java.util.UUID;

public record ReceiptResponse(
        UUID paymentId,
        String fileName,
        String contentType,
        String receiptContent
) {
}
