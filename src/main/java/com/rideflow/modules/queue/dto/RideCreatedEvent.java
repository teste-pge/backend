package com.rideflow.modules.queue.dto;

import java.time.Instant;
import java.util.UUID;

public record RideCreatedEvent(
        UUID rideId,
        UUID userId,
        String originDisplay,
        String destinationDisplay,
        String status,
        Instant createdAt) {

}
