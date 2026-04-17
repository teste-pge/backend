package com.rideflow.modules.ride.usecase.command;

import com.rideflow.modules.queue.dto.RideCreatedEvent;
import com.rideflow.modules.queue.facade.QueueFacade;
import com.rideflow.modules.ride.domain.Address;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.mapper.RideMapper;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.application.usecase.UseCase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateRideUseCase implements UseCase<CreateRideCommand, Ride> {

    private final RideRepository rideRepository;
    private final RideMapper rideMapper;
    private final QueueFacade queueFacade;

    @Override
    @Transactional
    public Ride execute(CreateRideCommand input) {
        final Address origin = rideMapper.toAddress(input.origin());
        final Address destination = rideMapper.toAddress(input.destination());

        final Ride ride = Ride.create(input.userId(), origin, destination);
        final Ride saved = rideRepository.saveAndFlush(ride);

        queueFacade.publishRideCreated(new RideCreatedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getOrigin().toDisplayString(),
                saved.getDestination().toDisplayString(),
                saved.getStatus().name(),
                saved.getCreatedAt()
        ));
        log.info("Ride {} created and published to Kafka", saved.getId());

        return saved;
    }
}
