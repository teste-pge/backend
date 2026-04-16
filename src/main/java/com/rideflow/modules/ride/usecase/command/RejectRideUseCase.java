package com.rideflow.modules.ride.usecase.command;

import com.rideflow.modules.queue.dto.RideRejectedEvent;
import com.rideflow.modules.queue.facade.QueueFacade;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.application.usecase.VoidUseCase;
import com.rideflow.shared.exception.RideNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectRideUseCase implements VoidUseCase<RejectRideCommand> {

    private final RideRepository rideRepository;
    private final QueueFacade queueFacade;

    @Override
    public void execute(RejectRideCommand input) {
        Ride ride = rideRepository.findById(
                input.rideId()
        ).orElseThrow(() -> new RideNotFoundException(input.rideId().toString()));

        queueFacade.publishRideRejected(new RideRejectedEvent(
                ride.getId(),
                input.driverId(),
                Instant.now()
        ));

        log.info(
                "Ride {} rejected by driver {}. Status remains {}.",
                ride.getId(),
                input.driverId(),
                ride.getStatus()
        );
    }
}
