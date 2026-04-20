package com.agileactors.pitfalls.model;

import java.time.Instant;

public record OddsMessage(
    long id,
    long eventId,
    long marketId,
    double homeOdds,
    double drawOdds,
    double awayOdds,
    Instant timestamp
) {}
