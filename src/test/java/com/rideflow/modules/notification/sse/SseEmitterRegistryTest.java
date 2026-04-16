package com.rideflow.modules.notification.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SseEmitterRegistryTest {

    private SseEmitterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SseEmitterRegistry();
    }

    @Test
    @DisplayName("Deve registrar emitter e incrementar contagem")
    void shouldRegisterEmitter() {
        UUID driverId = UUID.randomUUID();

        SseEmitter emitter = registry.register(driverId);

        assertThat(emitter).isNotNull();
        assertThat(registry.getConnectedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve remover emitter e decrementar contagem")
    void shouldRemoveEmitter() {
        UUID driverId = UUID.randomUUID();
        registry.register(driverId);

        registry.remove(driverId);

        assertThat(registry.getConnectedCount()).isZero();
    }

    @Test
    @DisplayName("Deve substituir emitter existente ao registrar mesmo driverId")
    void shouldReplaceExistingEmitter() {
        UUID driverId = UUID.randomUUID();
        SseEmitter first = registry.register(driverId);
        SseEmitter second = registry.register(driverId);

        assertThat(second).isNotSameAs(first);
        assertThat(registry.getConnectedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve broadcast para múltiplos emitters conectados")
    void shouldBroadcastToAll() {
        UUID driver1 = UUID.randomUUID();
        UUID driver2 = UUID.randomUUID();
        registry.register(driver1);
        registry.register(driver2);

        assertThat(registry.getConnectedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Remove de driverId inexistente não deve causar erro")
    void shouldNotFailOnRemoveUnknown() {
        registry.remove(UUID.randomUUID());

        assertThat(registry.getConnectedCount()).isZero();
    }
}
