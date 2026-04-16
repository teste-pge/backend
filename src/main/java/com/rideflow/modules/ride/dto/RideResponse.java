package com.rideflow.modules.ride.dto;

import com.rideflow.modules.ride.domain.RideStatus;

import java.time.Instant;
import java.util.UUID;

public record RideResponse(
        UUID id,
        UUID userId,
        UUID driverId,
        AddressResponse origin,
        AddressResponse destination,
        RideStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant acceptedAt) {

}
