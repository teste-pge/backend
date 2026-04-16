package com.rideflow.modules.ride.usecase.query;

import com.rideflow.modules.cache.facade.CacheFacade;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.application.usecase.UseCase;
import com.rideflow.shared.exception.RideNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FindRideByIdUseCase implements UseCase<UUID, Ride> {

    private final RideRepository rideRepository;
    private final CacheFacade cacheFacade;

    @Override
    public Ride execute(UUID rideId) {
        return cacheFacade.findRideInProgress(
                rideId
        ).filter(Ride.class::isInstance).map(Ride.class::cast).map(ride -> {
            log.info("Cache HIT for ride {}", rideId);
            return ride;
        }).orElseGet(() -> {
            log.info("Cache MISS for ride {}, fetching from DB", rideId);
            return rideRepository.findById(rideId)
                    .orElseThrow(() -> new RideNotFoundException(rideId.toString()));
        });
    }
}
