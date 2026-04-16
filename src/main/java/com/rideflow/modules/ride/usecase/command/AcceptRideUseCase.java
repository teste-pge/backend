package com.rideflow.modules.ride.usecase.command;

import com.rideflow.modules.cache.facade.CacheFacade;
import com.rideflow.modules.queue.dto.RideAcceptedEvent;
import com.rideflow.modules.queue.facade.QueueFacade;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.application.usecase.UseCase;
import com.rideflow.shared.exception.RideAlreadyAcceptedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptRideUseCase implements UseCase<AcceptRideCommand, Ride> {

    private final RideRepository rideRepository;
    private final QueueFacade queueFacade;
    private final CacheFacade cacheFacade;

    @Override
    @Transactional
    public Ride execute(AcceptRideCommand input) {
        Ride ride = rideRepository.findPendingRideForUpdate(input.rideId())
                .orElseThrow(() -> new RideAlreadyAcceptedException(input.rideId().toString()));

        ride.accept(input.driverId());
        final Ride saved = rideRepository.save(ride);

        cacheFacade.saveRideInProgress(saved.getId(), saved);
        log.info("Ride {} cached in Redis as in-progress", saved.getId());

        queueFacade.publishRideAccepted(new RideAcceptedEvent(
                saved.getId(),
                saved.getDriverId(),
                saved.getAcceptedAt()
        ));
        log.info("Ride {} accepted by driver {} and published to Kafka", saved.getId(), saved.getDriverId());

        return saved;
    }
}
