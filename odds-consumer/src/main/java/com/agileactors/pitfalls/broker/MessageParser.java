package com.agileactors.pitfalls.broker;

import com.agileactors.pitfalls.model.OddsChange;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageParser {

    private static final String EXPECTED_SCHEMA_VERSION = "1.0.0";
    private static final Set<String> EXPECTED_FIELDS = Set.of(
        "id", "eventId", "marketId", "homeOdds", "drawOdds", "awayOdds", "timestamp"
    );

    private final ObjectMapper objectMapper;

    public OddsChange parseMessage(Message message) throws IOException {
        // 1. Check schema version from headers
        String schemaVersion = message.getMessageProperties().getHeader("schema-version");

        if (schemaVersion == null) {
            log.warn("No schema version provided by producer");
        } else if (!EXPECTED_SCHEMA_VERSION.equals(schemaVersion)) {
            log.warn("Schema version mismatch. Expected: {}, Received: {}",
                EXPECTED_SCHEMA_VERSION, schemaVersion);
        }

        // 2. Parse once as Map to check fields
        String json = new String(message.getBody());
        Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {
        });

        Set<String> unknownFields = data.keySet().stream()
            .filter(field -> !EXPECTED_FIELDS.contains(field))
            .collect(Collectors.toSet());

        if (!unknownFields.isEmpty()) {
            log.warn("Unknown fields detected from producer: {}", unknownFields);
        }

        // 3. Convert Map to OddsChange (no re-parsing)
        return objectMapper.convertValue(data, OddsChange.class);
    }
}
