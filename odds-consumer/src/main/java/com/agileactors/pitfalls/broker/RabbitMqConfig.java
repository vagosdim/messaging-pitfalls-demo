package com.agileactors.pitfalls.broker;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMqConfig {

    private final RabbitMqProperties properties;

    @Bean
    public Queue oddsQueue() {
        return QueueBuilder.durable(properties.getQueueName())
            .deadLetterExchange(properties.getDlxName())
            .deadLetterRoutingKey(properties.getDlqRoutingKey())
            .build();
    }

    @Bean
    public Queue oddsDlq() {
        return QueueBuilder.durable(properties.getDlqName()).build();
    }

    @Bean
    public DirectExchange oddsExchange() {
        return new DirectExchange(properties.getExchangeName());
    }

    /**
     * P5 - Silent ACK Message Lost <br>
     * Log the event, and route the message to DLQ
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(properties.getDlxName());
    }

    @Bean
    public Binding binding(Queue oddsQueue, DirectExchange oddsExchange) {
        return BindingBuilder.bind(oddsQueue).to(oddsExchange).with(properties.getRoutingKey());
    }

    @Bean
    public Binding dlqBinding(Queue oddsDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(oddsDlq).to(deadLetterExchange).with(properties.getDlqRoutingKey());
    }
}
