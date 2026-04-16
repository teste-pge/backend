package com.rideflow.shared.metrics;

import com.rideflow.modules.notification.sse.SseEmitterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class RideMetricsService {

    private final Counter ridesCreatedCounter;
    private final Counter ridesAcceptedCounter;
    private final Counter ridesRejectedCounter;
    private final Counter kafkaPublishFailuresCounter;

    public RideMetricsService(MeterRegistry registry, SseEmitterRegistry emitterRegistry) {
        this.ridesCreatedCounter = Counter.builder("rides.created.total")
                .description("Total de corridas criadas")
                .register(registry);

        this.ridesAcceptedCounter = Counter.builder("rides.accepted.total")
                .description("Total de corridas aceitas")
                .register(registry);

        this.ridesRejectedCounter = Counter.builder("rides.rejected.total")
                .description("Total de corridas rejeitadas")
                .register(registry);

        this.kafkaPublishFailuresCounter = Counter.builder("kafka.publish.failures")
                .description("Total de falhas definitivas ao publicar no Kafka")
                .register(registry);

        Gauge.builder("sse.connections.active", emitterRegistry, SseEmitterRegistry::getConnectedCount)
                .description("Motoristas conectados via SSE")
                .register(registry);
    }

    public void incrementRidesCreated() {
        ridesCreatedCounter.increment();
    }

    public void incrementRidesAccepted() {
        ridesAcceptedCounter.increment();
    }

    public void incrementRidesRejected() {
        ridesRejectedCounter.increment();
    }

    public void incrementKafkaPublishFailures() {
        kafkaPublishFailuresCounter.increment();
    }
}
