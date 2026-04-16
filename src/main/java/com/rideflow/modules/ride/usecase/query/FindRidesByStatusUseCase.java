package com.rideflow.modules.ride.usecase.query;

import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.application.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FindRidesByStatusUseCase implements UseCase<FindRidesByStatusQuery, Page<Ride>> {

    private final RideRepository rideRepository;

    @Override
    public Page<Ride> execute(FindRidesByStatusQuery input) {
        return rideRepository.findByStatus(input.status(), input.pageable());
    }
}
