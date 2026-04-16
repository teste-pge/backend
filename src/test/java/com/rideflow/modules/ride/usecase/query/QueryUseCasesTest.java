package com.rideflow.modules.ride.usecase.query;

import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.domain.RideStatus;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.shared.exception.RideNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryUseCasesTest {

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private FindRideByIdUseCase findRideByIdUseCase;

    @InjectMocks
    private FindRidesByStatusUseCase findRidesByStatusUseCase;

    @InjectMocks
    private FindRidesByUserUseCase findRidesByUserUseCase;

    @Test
    void findById_withExistingId_shouldReturn() {
        UUID rideId = UUID.randomUUID();
        Ride ride = Ride.builder().id(rideId).build();

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        Ride result = findRideByIdUseCase.execute(rideId);

        assertThat(result.getId()).isEqualTo(rideId);
    }

    @Test
    void findById_withNonExistingId_shouldThrowNotFound() {
        UUID rideId = UUID.randomUUID();

        when(rideRepository.findById(rideId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findRideByIdUseCase.execute(rideId))
                .isInstanceOf(RideNotFoundException.class);
    }

    @Test
    void findByStatus_shouldDelegateToPaginatedRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        Ride ride = Ride.builder().status(RideStatus.PENDING).build();
        Page<Ride> page = new PageImpl<>(List.of(ride));

        when(rideRepository.findByStatus(RideStatus.PENDING, pageable)).thenReturn(page);

        Page<Ride> result = findRidesByStatusUseCase.execute(
                new FindRidesByStatusQuery(RideStatus.PENDING, pageable));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getStatus()).isEqualTo(RideStatus.PENDING);
    }
}
