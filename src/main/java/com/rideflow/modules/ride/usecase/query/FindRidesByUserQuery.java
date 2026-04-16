package com.rideflow.modules.ride.usecase.query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record FindRidesByUserQuery(UUID userId, Pageable pageable) {

}
