package com.rideflow.modules.ride.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DriverActionRequest(
        @NotNull
        UUID driverId) {

}
