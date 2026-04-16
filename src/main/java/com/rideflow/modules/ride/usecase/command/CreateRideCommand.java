package com.rideflow.modules.ride.usecase.command;

import com.rideflow.modules.ride.dto.AddressRequest;

import java.util.UUID;

public record CreateRideCommand(
        UUID userId,
        AddressRequest origin,
        AddressRequest destination) {

}
