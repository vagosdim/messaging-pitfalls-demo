package com.agileactors.validator.service;

import com.agileactors.validator.dto.OddsValidationRequest;
import com.agileactors.validator.dto.OddsValidationResponse;

/**
 * Service for validating betting odds and detecting sure bet opportunities.
 *
 * <p>A sure bet (arbitrage betting) occurs when the sum of implied probabilities
 * across all outcomes is less than 100%, allowing guaranteed profit regardless of outcome.
 *
 * <p>Formula: 1/homeOdds + 1/drawOdds + 1/awayOdds < 1.0 indicates a sure bet
 */
public interface OddsValidator {

    /**
     * Validates odds for potential sure bet scenarios.
     *
     * <p>Calculates implied probability and determines if odds represent a sure bet opportunity.
     * Intentionally slow (3 sec delay) to simulate external service latency.
     *
     * @param oddsValidationRequest the odds data to validate
     * @return validation response indicating if odds are valid and margin percentage
     * @throws InterruptedException if validation is interrupted
     */
    OddsValidationResponse validateOdds(OddsValidationRequest oddsValidationRequest) throws InterruptedException;

}
