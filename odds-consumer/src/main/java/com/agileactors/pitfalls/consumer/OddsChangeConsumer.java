package com.agileactors.pitfalls.consumer;

import com.agileactors.pitfalls.model.OddsChange;
import com.agileactors.pitfalls.model.OddsValidationResponse;
import com.agileactors.pitfalls.repository.OddsChangeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            String oddsJson = new String(message.getBody(), StandardCharsets.UTF_8);
            OddsChange oddsChange = parseOddsChange(oddsJson);
            if (!areOddsValid(oddsChange, channel, deliveryTag)) {
                return;
            }
            saveOddsChange(oddsChange);
            log.info("Successfully processed OddsChange with messageId={}, deliveryTag={}", oddsChange.getId(),
                deliveryTag);
            channel.basicAck(deliveryTag, false);
        } catch (JsonProcessingException e) {
            /* P1 - Infinite Loop
             * Log error, reject message, and don't requeue to avoid infinite loop
             */
            log.error("Failed to parse message, dropping. deliveryTag={}, error={}", deliveryTag, e.getMessage());
            channel.basicReject(deliveryTag, false);

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

    private boolean areOddsValid(OddsChange oddsChange, Channel channel, long deliveryTag) throws IOException {
        log.info("Validating odds for marketId={} with external service...", oddsChange.getMarketId());
        OddsValidationResponse validation = restClient.post()
            .uri("/validate-odds")
            .body(oddsChange)
            .retrieve()
            .body(OddsValidationResponse.class);

        if (!validation.valid()) {
            log.warn(
                "OddsChange={} with home={}, draw={}, away={}, has sure bet detected (margin: {}%), discarding",
                oddsChange.getId(), oddsChange.getHomeOdds(), oddsChange.getDrawOdds(), oddsChange.getAwayOdds(),
                validation.margin());
            channel.basicAck(deliveryTag, false);
            return false;
        }
        return true;
    }

    private void saveOddsChange(OddsChange oddsChange) {
        oddsChangeRepository.save(oddsChange);
    }

}
