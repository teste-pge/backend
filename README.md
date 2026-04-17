# RideFlow — Backend

> Sistema de corridas em tempo real com event-driven architecture | Java 21 + Spring Boot 3.2

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Kafka-3.6-231F20?logo=apachekafka)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?logo=postgresql)](https://www.postgresql.org/)
[![Tests](https://img.shields.io/badge/Tests-76%20green-brightgreen)]()

---

## Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                         RideFlow Backend                        │
│                                                                 │
│  ┌──────────┐   Facade   ┌──────────┐   Consumer  ┌──────────┐  │
│  │   ride   │──────────► │  queue   │──────────►  │  notif.  │  │
│  └──────────┘            └──────────┘             └──────────┘  │
│       │                       │                       │         │
│       │ Facade          ┌──────────┐                  │ SSE     │
│       └───────────────► │  cache   │                  ▼         │
│       │                 └──────────┘          [Motorista/Pass.] │
│       │ Facade                                                  │
│       ▼                                                         │
│  ┌──────────┐                                                   │
│  │  driver  │ (PostgreSQL + Flyway seed)                        │
│  └──────────┘                                                   │
│                                                                 │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                        shared                              │ │
│  │  exception/ · handler/ · response/ · validation/ · metrics/│ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

**Regra de ouro:** módulos se comunicam **exclusivamente via Facades** (interfaces).
Nunca importar implementações concretas entre módulos.

---

## Stack

| Tecnologia         | Versão  | Papel                                        |
|--------------------|---------|----------------------------------------------|
| Java               | 21 LTS  | Virtual Threads, Records, Pattern Matching   |
| Spring Boot        | 3.2.4   | Framework web                                |
| Spring Data JPA    | 3.2.x   | Persistência com `@Embedded`, `@Lock`        |
| Spring Kafka       | 3.1.x   | Producer com `@Retryable` + Consumer         |
| Spring Data Redis  | 3.2.x   | Cache-Aside pattern                          |
| PostgreSQL         | 15      | Enums nativos (`ride_status`, `driver_status`) |
| Flyway             | 9.x     | Migrations versionadas + seed de motoristas  |
| Apache Kafka       | 3.6.x   | KRaft mode (sem Zookeeper)                   |
| Redis              | 7       | Cache de corridas aceitas (TTL 4h)           |
| MapStruct          | 1.5.5   | Mapeamento DTO ↔ Entidade                    |
| Lombok             | 1.18.30 | Boilerplate reduction                        |
| SpringDoc/Swagger  | 2.3.0   | Documentação OpenAPI 3                       |
| TestContainers     | 1.21.0  | Testes de integração (PG + Kafka)            |
| JaCoCo             | 0.8.11  | Cobertura de código                          |
| Logback + Logstash | —       | JSON logging estruturado (perfil prod)       |

---

## Pré-requisitos

- **Java 21+** — `java -version`
- **Maven 3.9+** — `./mvnw -version`
- **Docker + Docker Compose** — para infraestrutura local e testes de integração

---

## Como rodar

### 1. Infraestrutura local (PostgreSQL + Redis + Kafka)

```bash
# Na raiz do repositório
docker compose up -d postgres redis kafka
```

> Kafka usa **KRaft** — sem Zookeeper.  
> Tópicos: `ride.created`, `ride.accepted`, `ride.rejected`, `ride.completed` (3 partições cada).

### 2. Aplicação

```bash
cd backend
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

### 3. Stack completa via Docker Compose

```bash
# Subir tudo (PostgreSQL + Redis + Kafka + Backend + Frontend)
docker compose up -d --build

# Ver logs do backend
docker logs -f rideflow-backend

# Parar tudo
docker compose down
```

### 4. Verificar saúde

```bash
curl http://localhost:8080/actuator/health
```

Retorna status de PostgreSQL, Redis e Kafka.

### 4. Swagger UI

Acesse [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html)

A documentação OpenAPI JSON está em `/api-docs`.

---

## Testes

O projeto possui **66 testes** (unitários + integração), todos verdes.

### Unitários

```bash
./mvnw test
```

### Com cobertura (JaCoCo)

```bash
./mvnw verify
# Relatório HTML em: target/site/jacoco/index.html
```

### Integração (TestContainers — requer Docker)

```bash
./mvnw verify -Dgroups=integration
```

Os testes de integração sobem PostgreSQL e Kafka via TestContainers automaticamente.

> **Nota:** Docker Engine 26+ requer `DOCKER_API_VERSION=1.45` (já configurado no Surefire).

---

## Fluxo de Eventos

```
1. POST /rides (criar corrida)
   └─► DB (PENDING) ─► Kafka [ride.created] ─► Consumer ─► SSE broadcast (todos motoristas)

2. POST /rides/{id}/accept (aceitar corrida)
   └─► Lock pessimista ─► DB (ACCEPTED) ─► Redis cache ─► Kafka [ride.accepted]
                                                          └─► Consumer ─► SSE (motorista específico)
                                                          └─► Consumer ─► SSE (passageiro: RIDE_ACCEPTED)

3. POST /rides/{id}/reject (rejeitar corrida)
   └─► Kafka [ride.rejected] ─► (status permanece PENDING para outros motoristas)

4. POST /rides/{id}/complete (finalizar corrida)
   └─► DB (COMPLETED) ─► Redis evict ─► Kafka [ride.completed]
                                         └─► Consumer ─► SSE (passageiro: RIDE_COMPLETED)

5. GET /rides/{id} (buscar corrida)
   └─► Redis (cache hit?) ─► sim: retorna direto
                           └─► não: PostgreSQL ─► retorna

6. GET /rides/active/user/{userId} (corrida ativa do passageiro)
   └─► DB (status IN PENDING, ACCEPTED) ─► retorna

7. GET /rides/active/driver/{driverId} (corrida ativa do motorista)
   └─► DB (status = ACCEPTED, driverId = ?) ─► retorna
```

---

## Variáveis de ambiente

| Variável                   | Padrão           | Descrição                  |
|----------------------------|------------------|----------------------------|
| `SPRING_PROFILES_ACTIVE`   | `dev`            | Perfil ativo               |
| `DB_HOST`                  | `localhost`      | Host do PostgreSQL         |
| `DB_PORT`                  | `5432`           | Porta do PostgreSQL        |
| `DB_NAME`                  | `rideflow`       | Nome do banco              |
| `DB_USER`                  | `rideflow`       | Usuário do banco           |
| `DB_PASS`                  | `rideflow`       | Senha do banco             |
| `REDIS_HOST`               | `localhost`      | Host do Redis              |
| `REDIS_PORT`               | `6379`           | Porta do Redis             |
| `REDIS_PASS`               | `rideflow`       | Senha do Redis             |
| `KAFKA_BOOTSTRAP_SERVERS`  | `localhost:9092` | Endereço do broker Kafka   |
| `SERVER_PORT`              | `8080`           | Porta HTTP                 |

Copie `.env.example` para `.env` e ajuste conforme necessário.

---

## Endpoints

> Documentação interativa completa no Swagger UI.

### Corridas

| Método | Endpoint                              | Descrição                          | Status  |
|--------|---------------------------------------|------------------------------------|---------|
| POST   | `/api/v1/rides`                       | Criar corrida                      | 201     |
| GET    | `/api/v1/rides/{rideId}`              | Buscar corrida por ID (cache-aside)| 200     |
| GET    | `/api/v1/rides?status=PENDING`        | Listar corridas por status         | 200     |
| GET    | `/api/v1/rides?userId={uuid}`         | Listar corridas por usuário        | 200     |
| POST   | `/api/v1/rides/{rideId}/accept`       | Motorista aceita corrida           | 200     |
| POST   | `/api/v1/rides/{rideId}/reject`       | Motorista rejeita corrida          | 200     |
| POST   | `/api/v1/rides/{rideId}/complete`     | Motorista finaliza corrida         | 200     |
| GET    | `/api/v1/rides/active/user/{userId}`  | Corrida ativa do passageiro        | 200     |
| GET    | `/api/v1/rides/active/driver/{driverId}` | Corrida ativa do motorista      | 200     |

### Motoristas

| Método | Endpoint                              | Descrição                          | Status  |
|--------|---------------------------------------|------------------------------------|---------|
| GET    | `/api/v1/drivers`                     | Listar todos os motoristas         | 200     |
| GET    | `/api/v1/drivers/available`           | Listar motoristas disponíveis      | 200     |
| GET    | `/api/v1/drivers/{driverId}`          | Buscar motorista por ID            | 200     |

### Notificações

| Método | Endpoint                                       | Descrição                    | Tipo     |
|--------|-------------------------------------------------|------------------------------|----------|
| GET    | `/api/v1/notifications/drivers/{driverId}/stream`    | SSE notificações do motorista  | SSE      |
| GET    | `/api/v1/notifications/passengers/{userId}/stream`   | SSE notificações do passageiro | SSE      |

### Observabilidade

| Método | Endpoint                    | Descrição                        |
|--------|-----------------------------|----------------------------------|
| GET    | `/actuator/health`          | Health check (PG, Redis, Kafka)  |
| GET    | `/actuator/info`            | Informações da aplicação         |
| GET    | `/actuator/metrics`         | Métricas Micrometer              |
| GET    | `/actuator/prometheus`      | Métricas formato Prometheus      |

---

## Métricas de Negócio

| Métrica                     | Tipo    | Descrição                         |
|-----------------------------|---------|-----------------------------------|
| `rides.created.total`       | Counter | Total de corridas criadas         |
| `rides.accepted.total`      | Counter | Total de corridas aceitas         |
| `rides.rejected.total`      | Counter | Total de corridas rejeitadas      |
| `rides.completed.total`     | Counter | Total de corridas finalizadas     |
| `sse.connections.active`    | Gauge   | Conexões SSE ativas (motoristas + passageiros) |
| `kafka.publish.failures`    | Counter | Falhas de publicação no Kafka     |
---

## Licença

Projeto de avaliação técnica — uso restrito.
