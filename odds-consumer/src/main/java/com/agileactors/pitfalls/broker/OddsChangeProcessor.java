package com.agileactors.pitfalls.broker;

import com.agileactors.pitfalls.model.OddsChange;
import com.agileactors.pitfalls.model.OddsValidationResponse;
import com.agileactors.pitfalls.repository.OddsChangeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OddsChangeProcessor implements ChannelAwareMessageListener {

    private final RestClient restClient;
    private final OddsChangeRepository oddsChangeRepository;
    private final ObjectMapper objectMapper;
    private final List<OddsChange> processingCache = new ArrayList<>(); // Memory leak

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getMessageId();

        log.info("Received odds change message, deliveryTag={}", deliveryTag);

        try {
            String oddsJson = new String(message.getBody());

            // PROBLEM 1: Strict deserialization - fails on unknown properties
            // If producer adds new field, ObjectMapper throws UnrecognizedPropertyException
            // Message becomes poison pill → infinite retry or lost
            // Should configure: FAIL_ON_UNKNOWN_PROPERTIES = false for backward compatibility
            OddsChange oddsChange = parseOddsChange(oddsJson);

            // PROBLEM 2: Blocking call in listener thread (3 sec)
            log.info("Validating odds for event {} with external service...", oddsChange.getEventId());
            OddsValidationResponse validation = restClient.post()
                .uri("/validate-odds")
                .body(oddsChange)
                .retrieve()
                .body(OddsValidationResponse.class);

            if (!validation.valid()) {
                // PROBLEM 3: No dead letter handling - business-rejected messages lost
                // RabbitMQ: Should route to Dead Letter Exchange (DLX) → DLQ
                // Kafka: Should publish to Dead Letter Topic (DLT)
                // Currently: ACK + discard = permanent data loss, no audit trail
                log.warn("Odds change {} has sure bet detected (margin: {}%), discarding",
                    oddsChange.getId(), validation.margin());
                channel.basicAck(deliveryTag, false);
                return;
            }

            // PROBLEM 4: No idempotency - duplicate processing
            log.info("Saving odds change {} to database", oddsChange.getId());
            oddsChangeRepository.save(oddsChange);

            // PROBLEM 5: No ordering guarantee - out-of-order processing

            // PROBLEM 6: Another blocking call (2 sec)
            log.info("Broadcasting odds change {} to clients", oddsChange.getId());
            restClient.post()
                .uri("/broadcast")
                .body(oddsChange)
                .retrieve()
                .toBodilessEntity();

            channel.basicAck(deliveryTag, false);
            log.info("Successfully processed odds change {}, deliveryTag={}", oddsChange.getId(), deliveryTag);

        } catch (RestClientException e) {
            // PROBLEM 7: No retry - immediate failure
            log.error("External service failed for deliveryTag={}, message lost", deliveryTag, e);
            channel.basicAck(deliveryTag, false); // ACK anyway = lost message

        } catch (Exception e) {
            // PROBLEM 8: Nack without strategy
            log.error("Processing failed for deliveryTag={}", deliveryTag, e);
            channel.basicNack(deliveryTag, false, true); // Infinite retry loop!
        }
    }

    private OddsChange parseOddsChange(String json) throws IOException {
        // PROBLEM 1 (continuation): Default ObjectMapper fails on unknown properties
        // Tight coupling between producer/consumer schemas
        // No backward compatibility when message format evolves
        return objectMapper.readValue(json, OddsChange.class);
    }
}
