package com.rideflow.modules.ride.usecase.query;

import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.application.usecase.UseCase;
import com.rideflow.shared.exception.RideNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FindRideByIdUseCase implements UseCase<UUID, Ride> {

    private final RideRepository rideRepository;

    @Override
    public Ride execute(UUID rideId) {
        return rideRepository.findById(
                rideId
        ).orElseThrow(() -> new RideNotFoundException(rideId.toString()));
    }
}
