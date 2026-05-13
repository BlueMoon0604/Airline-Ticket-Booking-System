package com.skybooker.payment.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange paymentExchange(
            @Value("${application.rabbitmq.payment-exchange}") String exchangeName
    ) {
        return new DirectExchange(exchangeName);
    }
}
