package com.rideflow.modules.ride.usecase.command;

import java.util.UUID;

public record RejectRideCommand(UUID rideId, UUID driverId) {

}
