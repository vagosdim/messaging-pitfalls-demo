package com.agileactors.pitfalls.entity;

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
@Table(name = "odds_changes")
public class OddsChangeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;

    @Column(unique = true, nullable = false)
    private Long marketId;

    private Double homeOdds;
    private Double drawOdds;
    private Double awayOdds;
    private Instant timestamp;
}
