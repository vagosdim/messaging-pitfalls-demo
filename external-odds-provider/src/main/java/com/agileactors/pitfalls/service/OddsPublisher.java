package com.agileactors.pitfalls.service;

import com.agileactors.pitfalls.broker.RabbitMqProperties;
import com.agileactors.pitfalls.model.OddsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OddsPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties properties;

    public void publishOdds(OddsMessage oddsMessage) {
        log.info("Publishing odds message: {}", oddsMessage);
        rabbitTemplate.convertAndSend(properties.getExchangeName(), properties.getRoutingKey(), oddsMessage);
    }

    public void publishOddsWithMessageId(OddsMessage oddsMessage, String messageId) {
        log.info("Publishing odds message with messageId={}: {}", messageId, oddsMessage);
        rabbitTemplate.convertAndSend(properties.getExchangeName(), properties.getRoutingKey(), oddsMessage,
            message -> {
                message.getMessageProperties().setMessageId(messageId);
                return message;
            });
    }
}
