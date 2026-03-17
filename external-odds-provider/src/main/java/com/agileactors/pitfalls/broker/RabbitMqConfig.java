package com.agileactors.pitfalls.broker;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMqConfig {

    private final RabbitMqProperties properties;

    @Bean
    public Queue oddsQueue() {
        return new Queue(properties.getQueueName(), true);
    }

    @Bean
    public DirectExchange oddsExchange() {
        return new DirectExchange(properties.getExchangeName(), true, false);
    }

    @Bean
    public Binding oddsBinding(Queue oddsQueue, DirectExchange oddsExchange) {
        return BindingBuilder.bind(oddsQueue)
            .to(oddsExchange)
            .with(properties.getRoutingKey());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
