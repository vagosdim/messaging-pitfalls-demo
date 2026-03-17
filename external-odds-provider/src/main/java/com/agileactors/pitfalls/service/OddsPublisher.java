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

    public void publishOdds(OddsMessage message) {
        log.info("Publishing odds message: {}", message);
        rabbitTemplate.convertAndSend(properties.getExchangeName(), properties.getRoutingKey(), message);
    }
}
