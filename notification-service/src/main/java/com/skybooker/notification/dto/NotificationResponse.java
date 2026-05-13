package com.skybooker.notification.dto;

import com.skybooker.notification.entity.NotificationChannel;
import com.skybooker.notification.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        UUID recipientId,
        NotificationType type,
        String title,
        String message,
        NotificationChannel channel,
        UUID relatedBookingId,
        Boolean isRead,
        LocalDateTime sentAt
) {
}
