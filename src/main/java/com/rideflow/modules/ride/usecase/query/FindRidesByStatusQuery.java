package com.rideflow.modules.ride.usecase.query;

import com.rideflow.modules.ride.domain.RideStatus;
import org.springframework.data.domain.Pageable;

public record FindRidesByStatusQuery(RideStatus status, Pageable pageable) {

}
