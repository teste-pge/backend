package com.rideflow.modules.ride.usecase.command;

import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.application.usecase.UseCase;
import com.rideflow.shared.exception.RideAlreadyAcceptedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AcceptRideUseCase implements UseCase<AcceptRideCommand, Ride> {

    private final RideRepository rideRepository;

    @Override
    @Transactional
    public Ride execute(AcceptRideCommand input) {
        Ride ride = rideRepository.findPendingRideForUpdate(input.rideId())
                .orElseThrow(() -> new RideAlreadyAcceptedException(input.rideId().toString()));

        ride.accept(input.driverId());

        return rideRepository.save(ride);
    }
}
