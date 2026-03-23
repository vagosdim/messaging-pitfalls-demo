# Docker Deployment Guide

### Build & Run (Recommended)
docker-compose up --build

---

## Service Endpoints

| Service | Port  | URL                                  |
|---------|-------|--------------------------------------|
| RabbitMQ AMQP | 5672  | `amqp://guest:guest@localhost:5672`  |
| RabbitMQ Management | 15672 | http://localhost:15672 (guest/guest) |
| odds-consumer | 8080  | http://localhost:8080                |
| external-odds-provider | 8081  | http://localhost:8081                |
| sure-bet-validator-api | 8082  | http://localhost:8082                |

---

## Service Communication
- **odds-consumer** → **sure-bet-validator-api**: `http://sure-bet-validator-api:8082`
- **odds-consumer** → **RabbitMQ**: `rabbitmq:5672`
- **external-odds-provider** → **RabbitMQ**: `rabbitmq:5672`
- **sure-bet-validator-api** → **RabbitMQ**: `rabbitmq:5672`

---

## Viewing Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f odds-consumer
docker-compose logs -f sure-bet-validator-api
docker-compose logs -f external-odds-provider
docker-compose logs -f rabbitmq
```

---
### Port Already in Use
```bash
lsof -i :8080
kill -9 <PID>
```

### Build Fails
```bash
# Clean and rebuild
docker-compose down
docker-compose up --build --no-cache
```




