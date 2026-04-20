package com.agileactors.validator.dto;

import java.time.Instant;

public record OddsValidationRequest(
    long id,
    long eventId,
    long marketId,
    double homeOdds,
    double drawOdds,
    double awayOdds,
    Instant timestamp
) {}
