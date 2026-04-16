package com.rideflow.modules.driver.dto;

import com.rideflow.modules.driver.domain.DriverStatus;

import java.util.UUID;

public record DriverResponse(
        UUID id,
        String name,
        String vehiclePlate,
        DriverStatus status) {

}
