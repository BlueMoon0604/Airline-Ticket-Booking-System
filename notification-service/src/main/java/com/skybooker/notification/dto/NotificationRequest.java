package com.skybooker.notification.dto;

import com.skybooker.notification.entity.NotificationChannel;
import com.skybooker.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NotificationRequest(
        @NotNull UUID recipientId,
        @NotNull NotificationType type,
        @NotBlank String title,
        @NotBlank String message,
        @NotNull NotificationChannel channel,
        UUID relatedBookingId
) {
}
