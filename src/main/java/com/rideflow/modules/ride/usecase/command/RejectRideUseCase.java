package com.rideflow.modules.ride.usecase.command;

import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.application.usecase.VoidUseCase;
import com.rideflow.shared.exception.RideNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectRideUseCase implements VoidUseCase<RejectRideCommand> {

    private final RideRepository rideRepository;

    @Override
    public void execute(RejectRideCommand input) {
        Ride ride = rideRepository.findById(input.rideId())
                .orElseThrow(() -> new RideNotFoundException(input.rideId().toString()));

        log.info(
                "Ride {} rejected by driver {}. Status remains {}.",
                ride.getId(),
                input.driverId(),
                ride.getStatus()
        );
    }
}
