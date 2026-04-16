package com.rideflow.modules.ride.usecase.command;

import com.rideflow.modules.queue.facade.QueueFacade;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.domain.RideStatus;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.exception.RideNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RejectRideUseCaseTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private QueueFacade queueFacade;

    @InjectMocks
    private RejectRideUseCase rejectRideUseCase;

    @Test
    void execute_shouldNotChangeStatus_rideRemainsPending() {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        Ride ride = Ride.builder()
                .id(rideId)
                .status(RideStatus.PENDING)
                .build();

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        rejectRideUseCase.execute(new RejectRideCommand(rideId, driverId));

        assertThat(ride.getStatus()).isEqualTo(RideStatus.PENDING);
    }

    @Test
    void execute_withNonExistingRide_shouldThrowNotFound() {
        UUID rideId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        when(rideRepository.findById(rideId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rejectRideUseCase.execute(new RejectRideCommand(rideId, driverId)))
                .isInstanceOf(RideNotFoundException.class);
    }
}
