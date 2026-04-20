package com.agileactors.validator.service;

import com.agileactors.validator.dto.OddsValidationRequest;
import com.agileactors.validator.dto.OddsValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SureBetOddsValidator implements OddsValidator {

    private static final long VALIDATION_DELAY_MS = 3000;
    private static final double SURE_BET_THRESHOLD = 1.0;
    private static final double PERCENTAGE_MULTIPLIER = 100.0;
    private static final String VALID_MESSAGE = "Odds are valid";
    private static final String SURE_BET_MESSAGE = "Sure bet detected";

    public OddsValidationResponse validateOdds(OddsValidationRequest oddsValidationRequest) throws InterruptedException {
        log.info("Validating odds for event: {}, market: {}",
            oddsValidationRequest.eventId(), oddsValidationRequest.marketId());
        if (oddsValidationRequest.id() == 1000) {
            log.info("test");
            Thread.sleep(VALIDATION_DELAY_MS + 2);
        }
        Thread.sleep(VALIDATION_DELAY_MS);

        double impliedProbability = calculateImpliedProbability(oddsValidationRequest);
        boolean isValid = impliedProbability >= SURE_BET_THRESHOLD;
        double margin = (impliedProbability - SURE_BET_THRESHOLD) * PERCENTAGE_MULTIPLIER;

        log.info("Odds validation completed for event: {}, valid: {}, margin: {}%",
            oddsValidationRequest.eventId(), isValid, margin);
        return new OddsValidationResponse(isValid, isValid ? VALID_MESSAGE : SURE_BET_MESSAGE, margin);
    }

    private double calculateImpliedProbability(OddsValidationRequest oddsValidationRequest) {
        // Sure bet formula: (1/homeOdds + 1/drawOdds + 1/awayOdds) < 1 means sure bet exists
        return (1.0 / oddsValidationRequest.homeOdds()) +
            (1.0 / oddsValidationRequest.drawOdds()) +
            (1.0 / oddsValidationRequest.awayOdds());
    }
}
