package com.rideflow.modules.ride.mapper;

import com.rideflow.modules.ride.domain.Address;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.domain.RideStatus;
import com.rideflow.modules.ride.dto.AddressRequest;
import com.rideflow.modules.ride.dto.AddressResponse;
import com.rideflow.modules.ride.dto.RideResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RideMapperTest {

    private final RideMapper mapper = new RideMapperImpl();

    @Test
    void toAddressResponse_shouldMapAllFields() {
        Address address = new Address("01310-100", "Av Paulista", "1000", "Apto 42", "Bela Vista", "São Paulo", "SP");

        AddressResponse response = mapper.toAddressResponse(address);

        assertThat(response.cep()).isEqualTo("01310-100");
        assertThat(response.logradouro()).isEqualTo("Av Paulista");
        assertThat(response.numero()).isEqualTo("1000");
        assertThat(response.complemento()).isEqualTo("Apto 42");
        assertThat(response.bairro()).isEqualTo("Bela Vista");
        assertThat(response.cidade()).isEqualTo("São Paulo");
        assertThat(response.estado()).isEqualTo("SP");
        assertThat(response.displayString()).contains("Av Paulista");
    }

    @Test
    void toAddressResponse_null_shouldReturnNull() {
        assertThat(mapper.toAddressResponse(null)).isNull();
    }

    @Test
    void toAddress_shouldMapFromRequest() {
        AddressRequest request = new AddressRequest("01310-100", "Av Paulista", "1000", "Apto 42", "Bela Vista", "São Paulo", "SP");

        Address address = mapper.toAddress(request);

        assertThat(address.getCep()).isEqualTo("01310-100");
        assertThat(address.getLogradouro()).isEqualTo("Av Paulista");
        assertThat(address.getNumero()).isEqualTo("1000");
        assertThat(address.getComplemento()).isEqualTo("Apto 42");
        assertThat(address.getBairro()).isEqualTo("Bela Vista");
        assertThat(address.getCidade()).isEqualTo("São Paulo");
        assertThat(address.getEstado()).isEqualTo("SP");
    }

    @Test
    void toAddress_null_shouldReturnNull() {
        assertThat(mapper.toAddress(null)).isNull();
    }

    @Test
    void toRideResponse_shouldMapAllFields() {
        UUID rideId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Address origin = new Address("01310-100", "Av Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");
        Address dest = new Address("20040-020", "Av Rio Branco", "1", null, "Centro", "Rio de Janeiro", "RJ");

        Ride ride = Ride.builder()
                .id(rideId)
                .userId(userId)
                .origin(origin)
                .destination(dest)
                .status(RideStatus.PENDING)
                .build();

        RideResponse response = mapper.toRideResponse(ride);

        assertThat(response.id()).isEqualTo(rideId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.status()).isEqualTo(RideStatus.PENDING);
        assertThat(response.origin()).isNotNull();
        assertThat(response.destination()).isNotNull();
    }

    @Test
    void toRideResponse_null_shouldReturnNull() {
        assertThat(mapper.toRideResponse(null)).isNull();
    }
}
