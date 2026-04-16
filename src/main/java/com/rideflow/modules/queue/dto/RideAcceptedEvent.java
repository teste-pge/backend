package com.rideflow.modules.queue.dto;

import java.time.Instant;
import java.util.UUID;

public record RideAcceptedEvent(
        UUID rideId,
        UUID driverId,
        Instant acceptedAt) {

}
