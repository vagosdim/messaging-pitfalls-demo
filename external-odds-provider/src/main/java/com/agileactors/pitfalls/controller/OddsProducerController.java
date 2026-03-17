package com.agileactors.pitfalls.controller;

import com.agileactors.pitfalls.model.OddsMessage;
import com.agileactors.pitfalls.service.MessageLoader;
import com.agileactors.pitfalls.service.OddsPublisher;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/odds")
@RequiredArgsConstructor
public class OddsProducerController {

    private final OddsPublisher oddsPublisher;
    private final MessageLoader messageLoader;

    @PostMapping("/publish")
    public ResponseEntity<String> publishOdds(@RequestParam(defaultValue = "sample-odds.json") String filename) {
        try {
            OddsMessage message = messageLoader.loadMessage(filename);
            oddsPublisher.publishOdds(message);
            return ResponseEntity.ok("Message published successfully from " + filename);
        } catch (Exception e) {
            log.error("Failed to publish message", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/publish-bulk")
    public ResponseEntity<String> publishBulk(@RequestParam int count,
        @RequestParam(defaultValue = "sample-odds.json") String filename) {
        try {
            OddsMessage template = messageLoader.loadMessage(filename);
            for (int i = 0; i < count; i++) {
                OddsMessage message = new OddsMessage(
                    i,
                    template.getEventId() + "-" + i,
                    template.getMarketId(),
                    template.getHomeOdds(),
                    template.getDrawOdds(),
                    template.getAwayOdds(),
                    Instant.now()
                );
                oddsPublisher.publishOdds(message);
            }
            log.info("Published {} messages from {}", count, filename);
            return ResponseEntity.ok("Published " + count + " messages from " + filename);
        } catch (Exception e) {
            log.error("Failed to publish bulk messages", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Producer is running");
    }
}
