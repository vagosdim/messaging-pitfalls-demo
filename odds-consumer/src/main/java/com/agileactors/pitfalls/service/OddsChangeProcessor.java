package com.agileactors.pitfalls.service;

import com.agileactors.pitfalls.broker.MessageParseException;
import com.agileactors.pitfalls.broker.MessageParser;
import com.agileactors.pitfalls.consumer.Action;
import com.agileactors.pitfalls.model.OddsChange;
import com.agileactors.pitfalls.model.OddsValidationResponse;
import com.agileactors.pitfalls.repository.OddsChangeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
@RequiredArgsConstructor
public class OddsChangeProcessor {

    private final RestClient restClient;
    private final OddsChangeRepository oddsChangeRepository;
    private final MessageParser messageParser;

    public Action process(Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            OddsChange oddsChange = messageParser.parseMessage(message);

            if (!areOddsValid(oddsChange)) {
                log.warn("Odds change {} has sure bet detected, discarding", oddsChange.getId());
                return Action.ACK;
            }

            saveOddsChange(oddsChange);
            log.info("Successfully processed OddsChange with messageId={}, deliveryTag={}", oddsChange.getId(), deliveryTag);
            return Action.ACK;

        } catch (MessageParseException e) {
            /* P1 - Infinite Loop
             * Log error, reject message, and don't requeue to avoid infinite loop
             */
            log.error("Failed to parse message, dropping. deliveryTag={}, error={}", deliveryTag, e.getMessage());
            return Action.REJECT;

        } catch (RestClientException e) {
            log.error("External service failed for deliveryTag={}, message lost", deliveryTag, e);
            return Action.ACK;

        } catch (Exception e) {
            log.error("Processing failed for deliveryTag={}", deliveryTag, e);
            return Action.RETRY;
        }
    }

    private boolean areOddsValid(OddsChange oddsChange) {
        log.info("Validating odds for event {} with external service...", oddsChange.getEventId());
        OddsValidationResponse validation = restClient.post()
            .uri("/validate-odds")
            .body(oddsChange)
            .retrieve()
            .body(OddsValidationResponse.class);

        if (!validation.valid()) {
            log.warn("Odds change {} has sure bet detected (margin: {}%)",
                oddsChange.getId(), validation.margin());
            return false;
        }
        return true;
    }

    private void saveOddsChange(OddsChange oddsChange) {
        oddsChangeRepository.save(oddsChange);
    }
}
