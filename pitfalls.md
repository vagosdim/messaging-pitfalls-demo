# Messaging Pitfalls — Consumer Demo Summary

---

## Pitfall 1 — Malformed JSON → Infinite Loop

**Pitfall:** `JsonProcessingException` falls into generic catch → `basicNack(requeue=true)` → infinite loop, queue blocked. Additionally, no charset specified when reading message body — JVM default charset may corrupt special characters before Jackson even parses.

```java
// Problem 1 — no charset
String oddsJson = new String(message.getBody()); // JVM default charset

// Problem 2 — infinite loop
} catch (Exception e) {
    channel.basicNack(deliveryTag, false, true); // requeue=true forever
}
```

**Fix:**
```java
// Fix 1 — explicit charset
String oddsJson = new String(message.getBody(), StandardCharsets.UTF_8);

// Fix 2 — catch parse failures first
OddsChange oddsValidationRequest;
try {
    oddsValidationRequest = parseOddsChange(oddsJson);
} catch (JsonProcessingException e) {
    log.error("Malformed message dropped. deliveryTag={}", deliveryTag, e);
    channel.basicReject(deliveryTag, false); // drop, log, move on
    return;
}
```

**Demo:** Change `id` type from `long` to `String` in payload → watch infinite loop in logs. Then send `"eventId": "ËVENT-001"` → show corrupted value in DB without charset fix.

**Broker:** RabbitMQ requeues infinitely, Kafka stalls partition forever on same offset. Charset issue is broker-agnostic — bytes are bytes. ✅ Both.

---

## Pitfall 2 — Silent Schema Drift → Unknown Properties

**Pitfall:** Jackson silently ignores unknown fields by default. Producer adds a new field, consumer processes happily, schema evolution completely invisible — you only notice when business logic breaks downstream.

```java
// No configuration — silently ignores new fields
ObjectMapper objectMapper; // default behavior
```

**Fix:**
```java
// Strict — tight producer/consumer ownership
objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

// Tolerant — shared/public topics
} catch (UnrecognizedPropertyException e) {
    log.warn("Schema drift detected, unknown field '{}'. deliveryTag={}",
        e.getPropertyName(), deliveryTag);
    channel.basicReject(deliveryTag, false);
    return;
}
```

**Demo:** Add new field `"scope": "LIVE"` to payload — consumer processes without any warning. Enable strict mode — now fails fast and visibly.

**Broker:** Deserialization is entirely consumer-side, broker irrelevant — same silent failure on both. ✅ Both.

---

## Pitfall 3 — Blocking Consumer Thread → Throughput Collapse

**Pitfall:** Entire processing flow runs on the consumer thread. External service has `sleep(3)` — 20 messages = 60s minimum. Consumer thread frozen, queue backs up visibly in management console.

```java
// Everything blocks the consumer thread
OddsValidationResponse validation = restClient.post()
    .uri("/validate-odds")
    .body(oddsValidationRequest)
    .retrieve()
    .body(OddsValidationResponse.class); // 3s blocked here
```

**Fix:** Offload entire processing to executor — consumer thread freed immediately after submit.
```java
private final ExecutorService consumerExecutor = Executors.newFixedThreadPool(10);

@Override
public void onMessage(Message message, Channel channel) throws IOException {
    consumerExecutor.submit(() -> {
        try {
            // entire processing flow — parse, validate, save, ack/nack
        } catch (Exception e) {
            log.error("Processing failed", e);
        }
    });
}
```

> **Java 21 footnote:** `spring.threads.virtual.enabled=true` — no pool tuning needed, blocking becomes cheap at platform level.

**Demo:** Flood queue with 20 messages, show 1 message/3s throughput. Apply fix, show concurrent processing.

**Broker:** RabbitMQ consumer thread frozen, Kafka partition poll stalls. ✅ Both.

---

## Pitfall 4 — Out of Order Processing → Stale Data

**Pitfall:** Direct consequence of pitfall 3 fix. Two messages for same `eventId` dispatched to different threads — slower thread overwrites faster thread's result, stale odds in DB.

```
Thread-1: EVENT-001, homeOdds=2.5 → slow (3s in validator)
Thread-2: EVENT-001, homeOdds=1.8 → fast
Thread-2 finishes first → DB: homeOdds=1.8
Thread-1 finishes late → DB: homeOdds=2.5 ← wrong, stale
```

**Fix — modulo thread affinity:**
```java
private final int THREAD_COUNT = 10;
private final ExecutorService[] executors = new ExecutorService[THREAD_COUNT];

// init
for (int i = 0; i < THREAD_COUNT; i++) {
    executors[i] = Executors.newSingleThreadExecutor();
}

// same eventId always routes to same thread
int threadIndex = Math.abs(oddsValidationRequest.getEventId().hashCode() % THREAD_COUNT);
executors[threadIndex].submit(() -> { ... });
```

**Demo:** Add `sleep(3)` in validator for `homeOdds=2.5` only — no consumer code touched. Publish two messages for same `eventId` rapidly, show stale value in DB.

**Broker:** Any concurrent consumer loses ordering without affinity. Third party producers mean you can't rely on partition keys. ✅ Both.

---

## Pitfall 5 — Sure Bet Messages → Silently ACK'd and Lost

**Pitfall:** Messages failing a business rule are ACK'd and gone forever — no audit trail, no replay capability. In a betting context this is a regulatory risk.

```java
// current — lost forever
if (!validation.valid()) {
    channel.basicAck(deliveryTag, false); // ← gone
    return;
}
```

**Fix:**
```java
if (!validation.valid()) {
    log.warn("Sure bet detected, routing to DLQ. deliveryTag={}", deliveryTag);
    message.getMessageProperties().getHeaders().put("x-reject-reason", "sure-bet-detected");
    message.getMessageProperties().getHeaders().put("x-margin", validation.margin());
    channel.basicReject(deliveryTag, false); // → DLQ
    return;
}
```

**Demo:** Trigger sure bet validation response, show message disappears with no trace. Apply fix, show message lands in DLQ with headers intact.

**Broker:** RabbitMQ loses on ACK, Kafka loses on offset commit. DLQ concept exists in both. ✅ Both.

---

## Pitfall 6 — No Idempotency → Duplicate DB Rows

**Pitfall:** `messageId` is extracted on line 2 but never used anywhere. On redelivery or duplicate publish, `save()` runs again — duplicate rows, wrong odds, potential financial exposure.

```java
// extracted, then completely ignored
String messageId = message.getMessageProperties().getMessageId();
// ...
oddsChangeRepository.save(oddsValidationRequest); // runs again on redelivery
```

**Fix:**
```java
if (oddsChangeRepository.existsByMessageId(messageId)) {
    log.warn("Duplicate message, skipping. messageId={}", messageId);
    channel.basicAck(deliveryTag, false);
    return;
}
oddsChangeRepository.save(oddsValidationRequest);
```

**Demo:** Publish same message twice with same `messageId` — two identical rows in DB. Apply fix — second message skipped, one row only.

**Broker:** Both guarantee at-least-once delivery — redelivery on RabbitMQ crash, replay on Kafka rebalance. ✅ Both.

---

## Pitfall 7 — Naive In-Memory Deduplication → Redis

**Pitfall:** Developer attempts to fix (6) with in-memory Set — each attempt better than the last but all critically flawed.

```java
// Attempt 1 — not thread-safe
private final Set<String> processedIds = new HashSet<>();

// Attempt 2 — thread-safe but unbounded (OOM eventually) and lost on restart
private final Set<String> processedIds = ConcurrentHashMap.newKeySet();
```

**Fix progression:**
```java
// Attempt 3 — Caffeine: thread-safe + bounded, but lost on restart
private final Cache<String, Boolean> processedIds = Caffeine.newBuilder()
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .maximumSize(10_000)
    .build();

// Attempt 4 — Redis: thread-safe + bounded + survives restart ✅
Boolean isDuplicate = redisTemplate.hasKey("odds:processed:" + messageId);
if (Boolean.TRUE.equals(isDuplicate)) {
    log.warn("Duplicate detected, skipping. messageId={}", messageId);
    channel.basicAck(deliveryTag, false);
    return;
}
redisTemplate.opsForValue().set(
    "odds:processed:" + messageId, "1", Duration.ofHours(24));
```

| Attempt | Thread-safe | Bounded | Survives restart |
|---|---|---|---|
| `HashSet` | ❌ | ❌ | ❌ |
| `ConcurrentHashMap.newKeySet()` | ✅ | ❌ | ❌ |
| Caffeine | ✅ | ✅ | ❌ |
| Redis | ✅ | ✅ | ✅ |

**Demo:** Restart consumer after processing — show duplicates reappear with HashSet/Caffeine. Show Redis survives restart cleanly.

**Broker:** In-memory state loss is broker-agnostic — same problem and fix on both. ✅ Both.

---

## Demo Order Justification

| # | Pitfall | Category | Builds on |
|---|---|---|---|
| 1 | Malformed JSON + charset | Parse failure | Standalone |
| 2 | Schema drift | Observability | Follows parse naturally |
| 3 | Blocking thread | Performance | Introduce ExecutorService |
| 4 | Out of order | Ordering | Direct consequence of (3) |
| 5 | Sure bet lost | Business logic | First ACK/DLQ concept |
| 6 | No idempotency | Delivery guarantee | At-least-once bites you |
| 7 | Naive cache | Fix evolution | Natural continuation of (7) |
