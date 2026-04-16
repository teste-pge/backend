package com.rideflow.modules.ride.usecase.command;

import java.util.UUID;

public record AcceptRideCommand(UUID rideId, UUID driverId) {

}
