package com.rideflow.modules.notification.service;

import com.rideflow.modules.notification.sse.SseEmitterRegistry;
import com.rideflow.modules.queue.dto.RideAcceptedEvent;
import com.rideflow.modules.queue.dto.RideCompletedEvent;
import com.rideflow.modules.queue.dto.RideCreatedEvent;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SseEmitterRegistry emitterRegistry;
    private final RideRepository rideRepository;

    public void notifyAllDrivers(RideCreatedEvent event) {
        Map<String, Object> payload = Map.of(
                "rideId", event.rideId().toString(),
                "userId", event.userId().toString(),
                "originDisplay", event.originDisplay(),
                "destinationDisplay", event.destinationDisplay(),
                "status", event.status(),
                "createdAt", event.createdAt() != null ? event.createdAt().toString() : Instant.now().toString()
        );

        emitterRegistry.broadcast("NEW_RIDE", payload);
        log.info("Notificação NEW_RIDE enviada para {} motoristas conectados. rideId={}",
                emitterRegistry.getConnectedCount(), event.rideId());
    }

    public void notifyRideAccepted(RideAcceptedEvent event) {
        Map<String, Object> payload = Map.of(
                "rideId", event.rideId().toString(),
                "driverId", event.driverId().toString(),
                "acceptedAt", event.acceptedAt().toString()
        );

        emitterRegistry.sendToDriver(event.driverId(), "RIDE_ACCEPTED", payload);
        log.info("Notificação RIDE_ACCEPTED enviada para driverId={}. rideId={}",
                event.driverId(), event.rideId());
    }

    public void notifyPassengerRideAccepted(RideAcceptedEvent event) {
        UUID userId = findUserIdByRideId(event.rideId());
        if (userId == null) return;

        Map<String, Object> payload = Map.of(
                "rideId", event.rideId().toString(),
                "driverId", event.driverId().toString(),
                "status", "ACCEPTED",
                "acceptedAt", event.acceptedAt().toString()
        );

        emitterRegistry.sendToPassenger(userId, "RIDE_ACCEPTED", payload);
        log.info("Notificação RIDE_ACCEPTED enviada para passageiro userId={}. rideId={}",
                userId, event.rideId());
    }

    public void notifyPassengerRideCompleted(RideCompletedEvent event) {
        Map<String, Object> payload = Map.of(
                "rideId", event.rideId().toString(),
                "driverId", event.driverId().toString(),
                "status", "COMPLETED",
                "completedAt", event.completedAt().toString()
        );

        emitterRegistry.sendToPassenger(event.userId(), "RIDE_COMPLETED", payload);
        log.info("Notificação RIDE_COMPLETED enviada para passageiro userId={}. rideId={}",
                event.userId(), event.rideId());
    }

    private UUID findUserIdByRideId(UUID rideId) {
        return rideRepository.findById(rideId)
                .map(Ride::getUserId)
                .orElse(null);
    }
}
