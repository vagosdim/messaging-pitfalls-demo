package com.agileactors.pitfalls.consumer;

import com.agileactors.pitfalls.model.OddsChange;
import com.agileactors.pitfalls.model.OddsValidationResponse;
import com.agileactors.pitfalls.repository.OddsChangeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OddsChangeConsumer {

    private final RestClient restClient;
    private final OddsChangeRepository oddsChangeRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${app.rabbitmq.queue-name}")
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getMessageId();

        log.info("Received odds change message, deliveryTag={}", deliveryTag);

        try {
            String oddsJson = new String(message.getBody());
            OddsChange oddsChange = parseOddsChange(oddsJson);

            log.info("Validating odds for event {} with external service...", oddsChange.getEventId());
            OddsValidationResponse validation = restClient.post()
                .uri("/validate-odds")
                .body(oddsChange)
                .retrieve()
                .body(OddsValidationResponse.class);

            if (!validation.valid()) {
                log.warn("Odds change {} has sure bet detected (margin: {}%), discarding",
                    oddsChange.getId(), validation.margin());
                channel.basicAck(deliveryTag, false);
                return;
            }

            log.info("Saving odds change {} to database", oddsChange.getId());
            oddsChangeRepository.save(oddsChange);

            channel.basicAck(deliveryTag, false);
            log.info("Successfully processed odds change {}, deliveryTag={}", oddsChange.getId(), deliveryTag);

        } catch (RestClientException e) {
            log.error("External service failed for deliveryTag={}, message lost", deliveryTag, e);
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Processing failed for deliveryTag={}", deliveryTag, e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    private OddsChange parseOddsChange(String json) throws IOException {
        return objectMapper.readValue(json, OddsChange.class);
    }
}
