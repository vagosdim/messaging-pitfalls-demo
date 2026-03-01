# Messaging Pitfalls Demo

## Project Structure

### odds-consumer
RabbitMQ consumer application that processes odds changes with intentional messaging pitfalls.

**Run:** `cd odds-consumer && mvn spring-boot:run`
**Port:** 8080

### sure-bet-validator-api
External API that validates odds for sure bet scenarios and broadcasts changes.

**Run:** `cd sure-bet-validator-api && mvn spring-boot:run`
**Port:** 9090

## Running the Demo

1. Start RabbitMQ (Docker): `docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management`
2. Start sure-bet-validator-api: `cd sure-bet-validator-api && mvn spring-boot:run`
3. Start odds-consumer: `cd odds-consumer && mvn spring-boot:run`
4. Publish messages to RabbitMQ queue: `odds-changes.queue`

## Demonstrating Pitfalls

- Stop sure-bet-validator-api to simulate external service failures
- Send high volume of messages to demonstrate concurrency bottleneck
- Monitor memory usage to see cache leak
