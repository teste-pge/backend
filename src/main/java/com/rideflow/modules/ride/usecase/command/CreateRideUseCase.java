package com.rideflow.modules.ride.usecase.command;

import com.rideflow.modules.ride.domain.Address;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.mapper.RideMapper;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.application.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateRideUseCase implements UseCase<CreateRideCommand, Ride> {

    private final RideRepository rideRepository;
    private final RideMapper rideMapper;

    @Override
    public Ride execute(CreateRideCommand input) {
        final Address origin = rideMapper.toAddress(input.origin());
        final Address destination = rideMapper.toAddress(input.destination());

        final Ride ride = Ride.create(input.userId(), origin, destination);

        return rideRepository.save(ride);
    }
}
