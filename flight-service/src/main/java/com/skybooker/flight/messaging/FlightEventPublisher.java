package com.skybooker.flight.messaging;

import com.skybooker.flight.entity.Flight;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FlightEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public FlightEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${application.rabbitmq.flight-exchange}") String exchange,
            @Value("${application.rabbitmq.flight-routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishFlightStatusUpdated(Flight flight) {
        Map<String, Object> event = Map.of(
                "flightId", flight.getFlightId().toString(),
                "flightNumber", flight.getFlightNumber(),
                "originAirportCode", flight.getOriginAirportCode(),
                "destinationAirportCode", flight.getDestinationAirportCode(),
                "status", flight.getStatus().name()
        );

        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
