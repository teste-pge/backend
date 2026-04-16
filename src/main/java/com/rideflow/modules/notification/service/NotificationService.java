package com.rideflow.modules.notification.service;

import com.rideflow.modules.notification.sse.SseEmitterRegistry;
import com.rideflow.modules.queue.dto.RideAcceptedEvent;
import com.rideflow.modules.queue.dto.RideCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SseEmitterRegistry emitterRegistry;

    public void notifyAllDrivers(RideCreatedEvent event) {
        Map<String, Object> payload = Map.of(
                "rideId", event.rideId().toString(),
                "userId", event.userId().toString(),
                "originDisplay", event.originDisplay(),
                "destinationDisplay", event.destinationDisplay(),
                "status", event.status(),
                "createdAt", event.createdAt().toString()
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
}
