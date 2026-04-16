package com.rideflow.modules.ride.usecase.command;

import com.rideflow.modules.ride.domain.Address;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.domain.RideStatus;
import com.rideflow.modules.ride.dto.AddressRequest;
import com.rideflow.modules.ride.dto.CreateRideRequest;
import com.rideflow.modules.ride.mapper.RideMapper;
import com.rideflow.modules.ride.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateRideUseCaseTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private RideMapper rideMapper;

    @InjectMocks
    private CreateRideUseCase createRideUseCase;

    @Test
    void execute_withValidData_shouldPersistAndReturnWithPendingStatus() {
        AddressRequest origin = new AddressRequest(
                "01310-100", "Av Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");
        AddressRequest destination = new AddressRequest(
                "04543-907", "Av Faria Lima", "3477", null, "Itaim Bibi", "São Paulo", "SP");
        CreateRideRequest request = new CreateRideRequest(UUID.randomUUID(), origin, destination);

        Address originAddr = new Address("01310-100", "Av Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");
        Address destAddr = new Address("04543-907", "Av Faria Lima", "3477", null, "Itaim Bibi", "São Paulo", "SP");

        when(rideMapper.toAddress(origin)).thenReturn(originAddr);
        when(rideMapper.toAddress(destination)).thenReturn(destAddr);

        Ride savedRide = Ride.builder()
                .id(UUID.randomUUID())
                .userId(request.userId())
                .origin(originAddr)
                .destination(destAddr)
                .status(RideStatus.PENDING)
                .build();

        when(rideRepository.save(any(Ride.class))).thenReturn(savedRide);

        Ride result = createRideUseCase.execute(request);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RideStatus.PENDING);
        verify(rideMapper).toAddress(origin);
        verify(rideMapper).toAddress(destination);
        verify(rideRepository).save(any(Ride.class));
    }
}
