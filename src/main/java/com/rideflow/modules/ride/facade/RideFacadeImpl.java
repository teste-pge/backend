package com.rideflow.modules.ride.facade;

import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.domain.RideStatus;
import com.rideflow.modules.ride.dto.CreateRideRequest;
import com.rideflow.modules.ride.dto.RideResponse;
import com.rideflow.modules.ride.mapper.RideMapper;
import com.rideflow.modules.ride.usecase.command.AcceptRideCommand;
import com.rideflow.modules.ride.usecase.command.AcceptRideUseCase;
import com.rideflow.modules.ride.usecase.command.CreateRideCommand;
import com.rideflow.modules.ride.usecase.command.CreateRideUseCase;
import com.rideflow.modules.ride.usecase.command.RejectRideCommand;
import com.rideflow.modules.ride.usecase.command.RejectRideUseCase;
import com.rideflow.modules.ride.usecase.query.FindRideByIdUseCase;
import com.rideflow.modules.ride.usecase.query.FindRidesByStatusQuery;
import com.rideflow.modules.ride.usecase.query.FindRidesByStatusUseCase;
import com.rideflow.modules.ride.usecase.query.FindRidesByUserQuery;
import com.rideflow.modules.ride.usecase.query.FindRidesByUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RideFacadeImpl implements RideFacade {

    private final CreateRideUseCase createRideUseCase;
    private final AcceptRideUseCase acceptRideUseCase;
    private final RejectRideUseCase rejectRideUseCase;
    private final FindRideByIdUseCase findRideByIdUseCase;
    private final FindRidesByStatusUseCase findRidesByStatusUseCase;
    private final FindRidesByUserUseCase findRidesByUserUseCase;
    private final RideMapper rideMapper;

    @Override
    public RideResponse createRide(CreateRideRequest request) {
        final CreateRideCommand command = new CreateRideCommand(
                request.userId(), request.origin(), request.destination());
        final Ride ride = createRideUseCase.execute(command);

        return rideMapper.toRideResponse(ride);
    }

    @Override
    public RideResponse findById(UUID rideId) {
        final Ride ride = findRideByIdUseCase.execute(rideId);

        return rideMapper.toRideResponse(ride);
    }

    @Override
    public Page<RideResponse> findByStatus(RideStatus status, Pageable pageable) {
        return findRidesByStatusUseCase.execute(
                new FindRidesByStatusQuery(status, pageable)
        ).map(rideMapper::toRideResponse);
    }

    @Override
    public Page<RideResponse> findByUserId(UUID userId, Pageable pageable) {
        return findRidesByUserUseCase.execute(
                new FindRidesByUserQuery(userId, pageable)
        ).map(rideMapper::toRideResponse);
    }

    @Override
    public RideResponse acceptRide(UUID rideId, UUID driverId) {
        final Ride ride = acceptRideUseCase.execute(new AcceptRideCommand(rideId, driverId));

        return rideMapper.toRideResponse(ride);
    }

    @Override
    public void rejectRide(UUID rideId, UUID driverId) {
        rejectRideUseCase.execute(new RejectRideCommand(rideId, driverId));
    }
}
