package com.skybooker.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter rabbitMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(rabbitMessageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        return factory;
    }

    @Bean
    public DirectExchange paymentExchange(
            @Value("${application.rabbitmq.payment-exchange}") String exchangeName
    ) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue paymentQueue(
            @Value("${application.rabbitmq.payment-queue}") String queueName
    ) {
        return new Queue(queueName);
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue,
                                  DirectExchange paymentExchange,
                                  @Value("${application.rabbitmq.payment-routing-key}") String routingKey) {
        return BindingBuilder.bind(paymentQueue).to(paymentExchange).with(routingKey);
    }

    @Bean
    public DirectExchange flightExchange(
            @Value("${application.rabbitmq.flight-exchange}") String exchangeName
    ) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue flightQueue(
            @Value("${application.rabbitmq.flight-queue}") String queueName
    ) {
        return new Queue(queueName);
    }

    @Bean
    public Binding flightBinding(Queue flightQueue,
                                 DirectExchange flightExchange,
                                 @Value("${application.rabbitmq.flight-routing-key}") String routingKey) {
        return BindingBuilder.bind(flightQueue).to(flightExchange).with(routingKey);
    }
}
