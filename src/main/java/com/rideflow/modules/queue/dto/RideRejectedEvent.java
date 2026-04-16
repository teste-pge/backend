package com.rideflow.modules.queue.dto;

import java.time.Instant;
import java.util.UUID;

public record RideRejectedEvent(
        UUID rideId,
        UUID driverId,
        Instant rejectedAt) {

}
