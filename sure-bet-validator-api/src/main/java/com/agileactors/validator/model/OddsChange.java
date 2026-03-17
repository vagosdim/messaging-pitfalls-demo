package com.agileactors.validator.model;

import java.time.Instant;
import lombok.Data;

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
