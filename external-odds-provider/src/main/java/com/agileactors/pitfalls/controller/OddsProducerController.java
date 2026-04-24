package com.agileactors.pitfalls.controller;

import com.agileactors.pitfalls.api.ProducerResponse;
import com.agileactors.pitfalls.model.OddsMessage;
import com.agileactors.pitfalls.service.MessageLoader;
import com.agileactors.pitfalls.service.OddsPublisher;
import java.io.IOException;
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

    private static final long OFFSET = 1000L;

    private final OddsPublisher oddsPublisher;
    private final MessageLoader messageLoader;

    /**
     * P1 — Malformed JSON <br>
     * P2 — Schema drift  <br>
     * P5 — Sure bet lost <br>
     */
    @PostMapping("/publish")
    public ResponseEntity<ProducerResponse> publishOdds(@RequestParam(defaultValue = "sample-odds.json") String filename)
        throws IOException {
        OddsMessage message = messageLoader.loadMessage(filename);
        oddsPublisher.publishOdds(message);
        return ResponseEntity.ok(ProducerResponse.ok("Message published successfully from " + filename));
    }

    /**
     * P3 — Blocking consumer thread (?count=20)
     */
    @PostMapping("/publish-bulk")
    public ResponseEntity<ProducerResponse> publishBulk(@RequestParam int count,
        @RequestParam(defaultValue = "sample-odds.json") String filename) throws IOException {
        OddsMessage oddsMessage = messageLoader.loadMessage(filename);
        for (int i = 0; i < count; i++) {
            OddsMessage message = new OddsMessage(
                oddsMessage.id() + i,
                i + OFFSET,
                oddsMessage.marketId(),
                oddsMessage.homeOdds(),
                oddsMessage.drawOdds(),
                oddsMessage.awayOdds(),
                Instant.now()
            );
            oddsPublisher.publishOdds(message);
        }
        log.info("Published {} messages from {}", count, filename);
        return ResponseEntity.ok(ProducerResponse.ok("Published " + count + " messages from " + filename));
    }

    /**
     * P4 — Out-of-order processing
     */
    @PostMapping("/publish-out-of-order")
    public ResponseEntity<ProducerResponse> publishOutOfOrder() throws IOException {
        OddsMessage slow = messageLoader.loadMessage("p4-first.json");
        OddsMessage fast = messageLoader.loadMessage("p4-second.json");
        oddsPublisher.publishOdds(slow);
        oddsPublisher.publishOdds(fast);
        return ResponseEntity.ok(ProducerResponse.ok("Published 2 messages for same eventId (out-of-order demo)"));
    }

    /**
     * P6/P7 — No idempotency / Naive dedup
     */
    @PostMapping("/publish-duplicate")
    public ResponseEntity<ProducerResponse> publishDuplicate(
        @RequestParam(defaultValue = "sample-odds.json") String filename) throws IOException {
        OddsMessage oddsMessage = messageLoader.loadMessage(filename);
        String duplicateMessageId = "duplicate-id-001";
        oddsPublisher.publishOddsWithMessageId(oddsMessage, duplicateMessageId);
        oddsPublisher.publishOddsWithMessageId(oddsMessage, duplicateMessageId);
        return ResponseEntity.ok(ProducerResponse.ok("Published 2 duplicate messages with messageId=" + duplicateMessageId));
    }

    @GetMapping("/health")
    public ResponseEntity<ProducerResponse> health() {
        return ResponseEntity.ok(ProducerResponse.ok("Producer is running"));
    }
}
