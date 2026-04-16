package com.rideflow.modules.ride.usecase.command;

import com.rideflow.modules.cache.facade.CacheFacade;
import com.rideflow.modules.queue.facade.QueueFacade;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.domain.RideStatus;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.exception.RideAlreadyAcceptedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcceptRideUseCaseTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private QueueFacade queueFacade;

    @Mock
    private CacheFacade cacheFacade;

    @InjectMocks
    private AcceptRideUseCase acceptRideUseCase;

    @Test
    void execute_withPendingRide_shouldUpdateToAccepted() {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        Ride pendingRide = Ride.builder()
                .id(rideId)
                .status(RideStatus.PENDING)
                .build();

        when(rideRepository.findPendingRideForUpdate(rideId)).thenReturn(Optional.of(pendingRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        Ride result = acceptRideUseCase.execute(new AcceptRideCommand(rideId, driverId));

        assertThat(result.getStatus()).isEqualTo(RideStatus.ACCEPTED);
        assertThat(result.getDriverId()).isEqualTo(driverId);
        assertThat(result.getAcceptedAt()).isNotNull();
    }

    @Test
    void execute_withAlreadyAcceptedRide_shouldThrow409() {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        when(rideRepository.findPendingRideForUpdate(rideId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> acceptRideUseCase.execute(new AcceptRideCommand(rideId, driverId)))
                .isInstanceOf(RideAlreadyAcceptedException.class);
    }
}
