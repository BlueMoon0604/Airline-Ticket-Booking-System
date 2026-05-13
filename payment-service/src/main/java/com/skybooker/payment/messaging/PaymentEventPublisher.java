package com.skybooker.payment.messaging;

import com.skybooker.payment.entity.Payment;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public PaymentEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${application.rabbitmq.payment-exchange}") String exchange,
            @Value("${application.rabbitmq.payment-routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishPaymentCompleted(Payment payment) {
        Map<String, Object> event = Map.of(
                "paymentId", payment.getPaymentId().toString(),
                "bookingId", payment.getBookingId().toString(),
                "userId", payment.getUserId().toString(),
                "status", payment.getStatus().name(),
                "amount", payment.getAmount().toString()
        );

        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
