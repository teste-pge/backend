package com.rideflow.modules.ride.dto;

import com.rideflow.shared.validation.DifferentAddresses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@DifferentAddresses
public record CreateRideRequest(
        @NotNull
        UUID userId,
        @NotNull
        @Valid
        AddressRequest origin,
        @NotNull
        @Valid
        AddressRequest destination) {

}
