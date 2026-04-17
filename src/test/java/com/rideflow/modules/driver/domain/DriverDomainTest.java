package com.rideflow.modules.driver.domain;

import com.rideflow.shared.exception.DriverNotAvailableException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class DriverDomainTest {

    @Test
    void markBusy_whenAvailable_shouldChangeToBusy() {
        Driver driver = Driver.builder().id(UUID.randomUUID()).name("Carlos").vehiclePlate("ABC-1234").build();

        driver.markBusy();

        assertThat(driver.getStatus()).isEqualTo(DriverStatus.BUSY);
    }

    @Test
    void markBusy_whenAlreadyBusy_shouldThrow() {
        Driver driver = Driver.builder().id(UUID.randomUUID()).name("Carlos").vehiclePlate("ABC-1234").build();
        driver.markBusy();

        assertThatThrownBy(driver::markBusy)
                .isInstanceOf(DriverNotAvailableException.class);
    }

    @Test
    void markAvailable_shouldChangeToAvailable() {
        Driver driver = Driver.builder().id(UUID.randomUUID()).name("Carlos").vehiclePlate("ABC-1234").build();
        driver.markBusy();

        driver.markAvailable();

        assertThat(driver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
        assertThat(driver.isAvailable()).isTrue();
    }

    @Test
    void isAvailable_defaultShouldBeTrue() {
        Driver driver = Driver.builder().id(UUID.randomUUID()).name("Ana").vehiclePlate("DEF-5678").build();
        assertThat(driver.isAvailable()).isTrue();
    }

    @Test
    void equalsAndHashCode_shouldBeBasedOnId() {
        UUID id = UUID.randomUUID();
        Driver d1 = Driver.builder().id(id).name("Carlos").vehiclePlate("ABC").build();
        Driver d2 = Driver.builder().id(id).name("Ana").vehiclePlate("DEF").build();
        Driver d3 = Driver.builder().id(UUID.randomUUID()).name("Carlos").vehiclePlate("ABC").build();

        assertThat(d1).isEqualTo(d2);
        assertThat(d1).isNotEqualTo(d3);
        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
    }

    @Test
    void toString_shouldContainFields() {
        Driver driver = Driver.builder().id(UUID.randomUUID()).name("Carlos").vehiclePlate("ABC-1234").build();
        String str = driver.toString();
        assertThat(str).contains("Carlos");
        assertThat(str).contains("ABC-1234");
    }

    @Test
    void getters_shouldWork() {
        UUID id = UUID.randomUUID();
        Driver driver = Driver.builder().id(id).name("Carlos").vehiclePlate("ABC-1234").build();

        assertThat(driver.getId()).isEqualTo(id);
        assertThat(driver.getName()).isEqualTo("Carlos");
        assertThat(driver.getVehiclePlate()).isEqualTo("ABC-1234");
    }
}
