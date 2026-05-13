package com.skybooker.notification.controller;

import com.skybooker.notification.dto.*;
import com.skybooker.notification.entity.NotificationType;
import com.skybooker.notification.service.NotifService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotifResource {

    private final NotifService notifService;

    public NotifResource(NotifService notifService) {
        this.notifService = notifService;
    }

    @Operation(summary = "Send a single notification")
    @PostMapping("/send")
    public NotificationResponse send(@Valid @RequestBody NotificationRequest request) {
        return notifService.send(request);
    }

    @Operation(summary = "Send booking confirmation via app, email and SMS")
    @PostMapping("/booking-confirmation")
    public List<NotificationResponse> sendBookingConfirmation(
            @Valid @RequestBody BookingConfirmationRequest request
    ) {
        return notifService.sendBookingConfirmation(request);
    }

    @Operation(summary = "Send bulk notifications")
    @PostMapping("/send-bulk")
    public List<NotificationResponse> sendBulk(@Valid @RequestBody BulkNotificationRequest request) {
        return notifService.sendBulk(request);
    }

    @Operation(summary = "Get notifications by recipient")
    @GetMapping("/recipient/{recipientId}")
    public List<NotificationResponse> getByRecipient(@PathVariable UUID recipientId) {
        return notifService.getByRecipient(recipientId);
    }

    @Operation(summary = "Get all notifications")
    @GetMapping
    public List<NotificationResponse> getAll() {
        return notifService.getAll();
    }

    @Operation(summary = "Get notifications by type")
    @GetMapping("/type/{type}")
    public List<NotificationResponse> getByType(@PathVariable NotificationType type) {
        return notifService.getByType(type);
    }

    @Operation(summary = "Mark notification as read")
    @PutMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(@PathVariable UUID notificationId) {
        return notifService.markAsRead(notificationId);
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/recipient/{recipientId}/read-all")
    public List<NotificationResponse> markAllRead(@PathVariable UUID recipientId) {
        return notifService.markAllRead(recipientId);
    }

    @Operation(summary = "Get unread count")
    @GetMapping("/recipient/{recipientId}/unread-count")
    public long getUnreadCount(@PathVariable UUID recipientId) {
        return notifService.getUnreadCount(recipientId);
    }

    @Operation(summary = "Delete notification")
    @DeleteMapping("/{notificationId}")
    public ApiResponse deleteNotification(@PathVariable UUID notificationId) {
        notifService.deleteNotification(notificationId);
        return new ApiResponse("Notification deleted successfully");
    }
}
