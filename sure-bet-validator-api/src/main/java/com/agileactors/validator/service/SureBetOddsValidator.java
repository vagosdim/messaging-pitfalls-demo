package com.agileactors.validator.service;

import com.agileactors.validator.model.OddsChange;
import com.agileactors.validator.model.OddsValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SureBetOddsValidator implements OddsValidator {

    private static final long VALIDATION_DELAY_MS = 3000;
    private static final long BROADCAST_DELAY_MS = 2000;
    private static final double SURE_BET_THRESHOLD = 1.0;
    private static final double PERCENTAGE_MULTIPLIER = 100.0;
    private static final String VALID_MESSAGE = "Odds are valid";
    private static final String SURE_BET_MESSAGE = "Sure bet detected";

    public OddsValidationResponse validateOdds(OddsChange oddsChange) throws InterruptedException {
        log.info("Validating odds for event: {}, market: {}",
            oddsChange.getEventId(), oddsChange.getMarketId());
        Thread.sleep(VALIDATION_DELAY_MS);

        double impliedProbability = calculateImpliedProbability(oddsChange);
        boolean isValid = impliedProbability >= SURE_BET_THRESHOLD;
        double margin = (impliedProbability - SURE_BET_THRESHOLD) * PERCENTAGE_MULTIPLIER;

        log.info("Odds validation completed for event: {}, valid: {}, margin: {}%",
            oddsChange.getEventId(), isValid, margin);
        return new OddsValidationResponse(isValid, isValid ? VALID_MESSAGE : SURE_BET_MESSAGE, margin);
    }

    public void broadcastOddsChange(OddsChange oddsChange) throws InterruptedException {
        log.info("Broadcasting odds change to clients for event: {}", oddsChange.getEventId());
        Thread.sleep(BROADCAST_DELAY_MS);
        log.info("Odds change broadcasted for event: {}", oddsChange.getEventId());
    }

    private double calculateImpliedProbability(OddsChange oddsChange) {
        // Sure bet formula: (1/homeOdds + 1/drawOdds + 1/awayOdds) < 1 means sure bet exists
        return (1.0 / oddsChange.getHomeOdds()) +
            (1.0 / oddsChange.getDrawOdds()) +
            (1.0 / oddsChange.getAwayOdds());
    }
}
