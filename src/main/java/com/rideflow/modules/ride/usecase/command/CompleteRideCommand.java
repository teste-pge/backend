package com.rideflow.modules.ride.usecase.command;

import java.util.UUID;

public record CompleteRideCommand(UUID rideId, UUID driverId) {

}
