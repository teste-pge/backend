package com.rideflow.modules.driver.mapper;

import com.rideflow.modules.driver.domain.Driver;
import com.rideflow.modules.driver.domain.DriverStatus;
import com.rideflow.modules.driver.dto.DriverResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DriverMapperTest {

    private final DriverMapper mapper = new DriverMapperImpl();

    @Test
    void toDriverResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        Driver driver = Driver.builder()
                .id(id)
                .name("Carlos")
                .vehiclePlate("ABC-1234")
                .status(DriverStatus.AVAILABLE)
                .build();

        DriverResponse response = mapper.toDriverResponse(driver);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Carlos");
        assertThat(response.vehiclePlate()).isEqualTo("ABC-1234");
        assertThat(response.status()).isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    void toDriverResponse_null_shouldReturnNull() {
        assertThat(mapper.toDriverResponse(null)).isNull();
    }
}
