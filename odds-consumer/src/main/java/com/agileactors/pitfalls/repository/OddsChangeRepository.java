package com.agileactors.pitfalls.repository;

import com.agileactors.pitfalls.model.OddsChangeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OddsChangeRepository extends JpaRepository<OddsChangeEntity, Long> {

    Optional<OddsChangeEntity> findByMarketId(String marketId);
}
