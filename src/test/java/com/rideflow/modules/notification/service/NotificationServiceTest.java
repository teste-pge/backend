package com.rideflow.modules.notification.service;

import com.rideflow.modules.notification.sse.SseEmitterRegistry;
import com.rideflow.modules.queue.dto.RideAcceptedEvent;
import com.rideflow.modules.queue.dto.RideCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private SseEmitterRegistry emitterRegistry;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("Deve broadcast NEW_RIDE para todos os motoristas conectados")
    void shouldBroadcastNewRide() {
        RideCreatedEvent event = new RideCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Rua A, 123",
                "Rua B, 456",
                "WAITING_ACCEPTANCE",
                Instant.now()
        );
        when(emitterRegistry.getConnectedCount()).thenReturn(3);

        notificationService.notifyAllDrivers(event);

        verify(emitterRegistry).broadcast(eq("NEW_RIDE"), any());
    }

    @Test
    @DisplayName("Deve enviar RIDE_ACCEPTED para motorista específico")
    void shouldNotifyRideAccepted() {
        UUID driverId = UUID.randomUUID();
        RideAcceptedEvent event = new RideAcceptedEvent(
                UUID.randomUUID(),
                driverId,
                Instant.now()
        );

        notificationService.notifyRideAccepted(event);

        verify(emitterRegistry).sendToDriver(eq(driverId), eq("RIDE_ACCEPTED"), any());
    }
}
