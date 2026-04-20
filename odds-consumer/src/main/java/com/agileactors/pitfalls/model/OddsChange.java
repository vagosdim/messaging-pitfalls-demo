package com.agileactors.pitfalls.model;

import java.time.Instant;
import lombok.Data;

@Data
public class OddsChange {

    private long id;
    private long eventId;
    private long marketId;
    private double homeOdds;
    private double drawOdds;
    private double awayOdds;
    private Instant timestamp;
}
