package com.agileactors.pitfalls.broker;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
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
    public DirectExchange oddsExchange() {
        return new DirectExchange(properties.getExchangeName());
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(properties.getRoutingKey());
    }

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(
        ConnectionFactory connectionFactory,
        OddsChangeProcessor processor) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(properties.getQueueName());
        // With AUTO, if processing crashes after Spring ACKs but before you finishing processing, message is lost forever
        // With MANUAL, you ACK only after successful processing → if it crashes, RabbitMQ redelivers
        // Gives you control over basicNack + dead-letter routing on failure
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(processor);
        return container;
    }
}
