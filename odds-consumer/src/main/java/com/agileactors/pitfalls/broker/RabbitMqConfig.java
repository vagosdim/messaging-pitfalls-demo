package com.agileactors.pitfalls.broker;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
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
    public TopicExchange oddsExchange() {
        return new TopicExchange(properties.getExchangeName());
    }

    @Bean
    public Binding binding(Queue oddsQueue, TopicExchange oddsExchange) {
        return BindingBuilder.bind(oddsQueue).to(oddsExchange).with(properties.getRoutingKey());
    }

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(
        ConnectionFactory connectionFactory,
        OddsChangeProcessor processor) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(properties.getQueueName());
        container.setMessageListener(processor);
        return container;
    }
}
