package com.skybooker.notification.messaging;

import com.skybooker.notification.dto.BulkNotificationRequest;
import com.skybooker.notification.entity.NotificationChannel;
import com.skybooker.notification.entity.NotificationType;
import com.skybooker.notification.service.NotifService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentEventListener {

    private final NotifService notifService;

    public PaymentEventListener(NotifService notifService) {
        this.notifService = notifService;
    }

    @RabbitListener(queues = "${application.rabbitmq.flight-queue}")
    public void handleFlightStatusUpdated(Map<String, Object> event) {
        String status = event.get("status").toString();
        String flightNumber = event.get("flightNumber").toString();

        NotificationType type = switch (status) {
            case "DELAYED" -> NotificationType.FLIGHT_DELAY;
            case "CANCELLED" -> NotificationType.CANCELLATION;
            case "BOARDING" -> NotificationType.BOARDING;
            default -> NotificationType.GATE_CHANGE;
        };

        BulkNotificationRequest request = new BulkNotificationRequest(
                List.of(UUID.fromString("11111111-1111-1111-1111-111111111111")),
                type,
                "Flight Update",
                "Flight " + flightNumber + " status updated to " + status,
                NotificationChannel.APP,
                null
        );

        notifService.sendBulk(request);
    }
}
