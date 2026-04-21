package com.agileactors.pitfalls.service;

import com.agileactors.pitfalls.broker.RabbitMqProperties;
import com.agileactors.pitfalls.model.OddsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
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
        rabbitTemplate.convertAndSend(properties.getExchangeName(), properties.getRoutingKey(), oddsMessage,
            message -> addEventIdHeader(message, oddsMessage));
    }

    /**
     * P6 - Idempotency <br>
     * This method allows publishing messages with a specific messageId, which can be used by consumers to ensure idempotency.
     */
    public void publishOddsWithMessageId(OddsMessage oddsMessage, String messageId) {
        log.info("Publishing odds message with messageId={}: {}", messageId, oddsMessage);
        rabbitTemplate.convertAndSend(properties.getExchangeName(), properties.getRoutingKey(), oddsMessage,
            message -> {
                addEventIdHeader(message, oddsMessage);
                message.getMessageProperties().setMessageId(messageId);
                return message;
            });
    }

    /**
     * P4 - Stale Data <br>
     * EventId is added to the message header for routing purposes
     */
    private Message addEventIdHeader(Message message, OddsMessage oddsMessage) {
        message.getMessageProperties().setHeader("eventId", oddsMessage.eventId());
        return message;
    }
}

