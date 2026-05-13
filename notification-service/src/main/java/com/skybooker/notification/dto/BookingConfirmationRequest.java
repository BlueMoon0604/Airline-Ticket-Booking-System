package com.skybooker.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BookingConfirmationRequest(
        @NotNull UUID recipientId,
        @NotNull UUID relatedBookingId,
        @NotBlank String recipientEmail,
        @NotBlank String recipientPhone,
        @NotBlank String pnrCode,
        List<EmailAttachmentRequest> attachments
) {
}
