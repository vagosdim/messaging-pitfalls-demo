package com.agileactors.pitfalls.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OddsMessage {

    private long id;
    private String eventId;
    private String marketId;
    private double homeOdds;
    private double drawOdds;
    private double awayOdds;
    private Instant timestamp;
}
