package com.rideflow.shared.metrics;

import com.rideflow.modules.notification.sse.SseEmitterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RideMetricsServiceTest {

    private MeterRegistry registry;
    private RideMetricsService metricsService;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        SseEmitterRegistry emitterRegistry = new SseEmitterRegistry();
        metricsService = new RideMetricsService(registry, emitterRegistry);
    }

    @Test
    @DisplayName("Deve incrementar contador rides.created.total")
    void incrementRidesCreated_shouldIncrementCounter() {
        metricsService.incrementRidesCreated();
        metricsService.incrementRidesCreated();

        double count = registry.counter("rides.created.total").count();
        assertThat(count).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Deve incrementar contador rides.accepted.total")
    void incrementRidesAccepted_shouldIncrementCounter() {
        metricsService.incrementRidesAccepted();

        double count = registry.counter("rides.accepted.total").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Deve incrementar contador rides.rejected.total")
    void incrementRidesRejected_shouldIncrementCounter() {
        metricsService.incrementRidesRejected();

        double count = registry.counter("rides.rejected.total").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Deve registrar gauge sse.connections.active com valor 0")
    void sseConnectionsGauge_shouldReflectEmitterCount() {
        double active = registry.get("sse.connections.active").gauge().value();
        assertThat(active).isZero();
    }

    @Test
    @DisplayName("Deve incrementar contador kafka.publish.failures")
    void incrementKafkaPublishFailures_shouldIncrementCounter() {
        metricsService.incrementKafkaPublishFailures();
        metricsService.incrementKafkaPublishFailures();
        metricsService.incrementKafkaPublishFailures();

        double count = registry.counter("kafka.publish.failures").count();
        assertThat(count).isEqualTo(3.0);
    }
}
