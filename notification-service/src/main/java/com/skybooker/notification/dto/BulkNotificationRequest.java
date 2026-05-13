package com.skybooker.notification.dto;

import com.skybooker.notification.entity.NotificationChannel;
import com.skybooker.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BulkNotificationRequest(
        @NotEmpty List<UUID> recipientIds,
        @NotNull NotificationType type,
        @NotBlank String title,
        @NotBlank String message,
        @NotNull NotificationChannel channel,
        UUID relatedBookingId
) {
}
