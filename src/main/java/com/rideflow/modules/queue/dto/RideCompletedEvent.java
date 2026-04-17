package com.rideflow.modules.queue.dto;

import java.time.Instant;
import java.util.UUID;

public record RideCompletedEvent(
        UUID rideId,
        UUID userId,
        UUID driverId,
        Instant completedAt) {

}
