package com.rideflow.modules.ride.facade;

import com.rideflow.modules.ride.domain.RideStatus;
import com.rideflow.modules.ride.dto.CreateRideRequest;
import com.rideflow.modules.ride.dto.RideResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RideFacade {

    RideResponse createRide(CreateRideRequest request);

    RideResponse findById(UUID rideId);

    Page<RideResponse> findByStatus(RideStatus status, Pageable pageable);

    Page<RideResponse> findByUserId(UUID userId, Pageable pageable);

    RideResponse acceptRide(UUID rideId, UUID driverId);

    void rejectRide(UUID rideId, UUID driverId);
}
