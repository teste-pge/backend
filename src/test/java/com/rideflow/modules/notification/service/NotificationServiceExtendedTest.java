package com.rideflow.modules.notification.service;

import com.rideflow.modules.notification.sse.SseEmitterRegistry;
import com.rideflow.modules.queue.dto.RideAcceptedEvent;
import com.rideflow.modules.queue.dto.RideCompletedEvent;
import com.rideflow.modules.queue.dto.RideCreatedEvent;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceExtendedTest {

    @Mock
    private SseEmitterRegistry emitterRegistry;

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void notifyAllDrivers_shouldBroadcast() {
        UUID rideId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RideCreatedEvent event = new RideCreatedEvent(rideId, userId, "Origin", "Dest", "PENDING", Instant.now());

        notificationService.notifyAllDrivers(event);

        verify(emitterRegistry).broadcast(eq("NEW_RIDE"), any());
    }

    @Test
    void notifyAllDrivers_withNullCreatedAt_shouldUseNow() {
        UUID rideId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RideCreatedEvent event = new RideCreatedEvent(rideId, userId, "Origin", "Dest", "PENDING", null);

        notificationService.notifyAllDrivers(event);

        verify(emitterRegistry).broadcast(eq("NEW_RIDE"), any());
    }

    @Test
    void notifyRideAccepted_shouldSendToDriver() {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        RideAcceptedEvent event = new RideAcceptedEvent(rideId, driverId, Instant.now());

        notificationService.notifyRideAccepted(event);

        verify(emitterRegistry).sendToDriver(eq(driverId), eq("RIDE_ACCEPTED"), any());
    }

    @Test
    void notifyPassengerRideAccepted_shouldSendToPassenger() {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RideAcceptedEvent event = new RideAcceptedEvent(rideId, driverId, Instant.now());

        Ride ride = Ride.builder().id(rideId).userId(userId).build();
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        notificationService.notifyPassengerRideAccepted(event);

        verify(emitterRegistry).sendToPassenger(eq(userId), eq("RIDE_ACCEPTED"), any());
    }

    @Test
    void notifyPassengerRideAccepted_rideNotFound_shouldNotSend() {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        RideAcceptedEvent event = new RideAcceptedEvent(rideId, driverId, Instant.now());

        when(rideRepository.findById(rideId)).thenReturn(Optional.empty());

        notificationService.notifyPassengerRideAccepted(event);

        verify(emitterRegistry, never()).sendToPassenger(any(), any(), any());
    }

    @Test
    void notifyPassengerRideCompleted_shouldSendToPassenger() {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RideCompletedEvent event = new RideCompletedEvent(rideId, userId, driverId, Instant.now());

        notificationService.notifyPassengerRideCompleted(event);

        verify(emitterRegistry).sendToPassenger(eq(userId), eq("RIDE_COMPLETED"), any());
    }
}
