package com.agileactors.pitfalls.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Data;

@Data
@Entity
@Table(
    name = "odds_change_entity", uniqueConstraints = {
    @jakarta.persistence.UniqueConstraint(columnNames = {"marketId"})
})
public class OddsChangeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventId;
    @Column(nullable = false)
    private String marketId;
    private double homeOdds;
    private double drawOdds;
    private double awayOdds;
    private Instant timestamp;
}
