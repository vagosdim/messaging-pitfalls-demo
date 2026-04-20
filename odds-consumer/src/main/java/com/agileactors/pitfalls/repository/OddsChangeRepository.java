package com.agileactors.pitfalls.repository;

import com.agileactors.pitfalls.model.OddsChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class OddsChangeRepository {

    public void save(OddsChange oddsChange) {
        log.info("Saving OddsChange to database: {}", oddsChange);
    }
}
