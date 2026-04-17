package com.rideflow.modules.ride.usecase.command;

import com.rideflow.modules.cache.facade.CacheFacade;
import com.rideflow.modules.queue.dto.RideCompletedEvent;
import com.rideflow.modules.queue.facade.QueueFacade;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.application.usecase.UseCase;
import com.rideflow.shared.exception.RideNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteRideUseCase implements UseCase<CompleteRideCommand, Ride> {

    private final RideRepository rideRepository;
    private final QueueFacade queueFacade;
    private final CacheFacade cacheFacade;

    @Override
    @Transactional
    public Ride execute(CompleteRideCommand input) {
        Ride ride = rideRepository.findAcceptedRideForDriver(input.rideId(), input.driverId())
                .orElseThrow(() -> new RideNotFoundException(input.rideId().toString()));

        ride.complete();
        final Ride saved = rideRepository.save(ride);

        cacheFacade.evictRide(saved.getId());
        log.info("Ride {} evicted from Redis cache", saved.getId());

        queueFacade.publishRideCompleted(new RideCompletedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getDriverId(),
                saved.getUpdatedAt()
        ));
        log.info("Ride {} completed by driver {} and published to Kafka", saved.getId(), saved.getDriverId());

        return saved;
    }
}
