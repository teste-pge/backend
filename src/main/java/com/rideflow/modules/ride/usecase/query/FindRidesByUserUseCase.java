package com.rideflow.modules.ride.usecase.query;

import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.application.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FindRidesByUserUseCase implements UseCase<FindRidesByUserQuery, Page<Ride>> {

    private final RideRepository rideRepository;

    @Override
    public Page<Ride> execute(FindRidesByUserQuery input) {
        return rideRepository.findByUserId(input.userId(), input.pageable());
    }
}
