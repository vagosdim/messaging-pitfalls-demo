package com.agileactors.validator.model;

import lombok.Data;
import java.time.Instant;

@Data
public class OddsChange {
    private long id;
    private String eventId;
    private String marketId;
    private double homeOdds;
    private double drawOdds;
    private double awayOdds;
    private Instant timestamp;
}
