package com.skybooker.notification.service;

import com.skybooker.notification.dto.*;
import com.skybooker.notification.entity.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotifService {

    NotificationResponse send(NotificationRequest request);

    List<NotificationResponse> sendBulk(BulkNotificationRequest request);

    List<NotificationResponse> sendBookingConfirmation(BookingConfirmationRequest request);

    NotificationResponse markAsRead(UUID notificationId);

    List<NotificationResponse> markAllRead(UUID recipientId);

    List<NotificationResponse> getByRecipient(UUID recipientId);

    long getUnreadCount(UUID recipientId);

    void deleteNotification(UUID notificationId);
    List<NotificationResponse> getAll();

    List<NotificationResponse> getByType(NotificationType type);

    void sendEmail(String toEmail, String subject, String body, List<EmailAttachmentRequest> attachments);

    void sendSMS(String toPhone, String message);

    void sendCheckInReminders();
}
