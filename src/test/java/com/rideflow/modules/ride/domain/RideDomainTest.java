package com.rideflow.modules.ride.domain;

import com.rideflow.shared.exception.RideAlreadyAcceptedException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class RideDomainTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID driverId = UUID.randomUUID();

    private Address makeAddress(String logradouro) {
        return new Address("01310-100", logradouro, "100", null, "Bela Vista", "São Paulo", "SP");
    }

    @Test
    void create_shouldReturnRideWithPendingStatus() {
        Address origin = makeAddress("Av Paulista");
        Address dest = makeAddress("Av Rio Branco");

        Ride ride = Ride.create(userId, origin, dest);

        assertThat(ride.getUserId()).isEqualTo(userId);
        assertThat(ride.getOrigin()).isEqualTo(origin);
        assertThat(ride.getDestination()).isEqualTo(dest);
        assertThat(ride.getStatus()).isEqualTo(RideStatus.PENDING);
        assertThat(ride.getDriverId()).isNull();
    }

    @Test
    void accept_shouldChangeStatusAndSetDriver() {
        Ride ride = Ride.builder().id(UUID.randomUUID()).userId(userId).origin(makeAddress("A")).destination(makeAddress("B")).build();

        ride.accept(driverId);

        assertThat(ride.getStatus()).isEqualTo(RideStatus.ACCEPTED);
        assertThat(ride.getDriverId()).isEqualTo(driverId);
        assertThat(ride.getAcceptedAt()).isNotNull();
    }

    @Test
    void accept_whenNotPending_shouldThrow() {
        Ride ride = Ride.builder().id(UUID.randomUUID()).userId(userId).origin(makeAddress("A")).destination(makeAddress("B")).build();
        ride.accept(driverId);

        assertThatThrownBy(() -> ride.accept(UUID.randomUUID()))
                .isInstanceOf(RideAlreadyAcceptedException.class);
    }

    @Test
    void complete_shouldChangeStatusToCompleted() {
        Ride ride = Ride.builder().id(UUID.randomUUID()).userId(userId).origin(makeAddress("A")).destination(makeAddress("B")).build();
        ride.accept(driverId);

        ride.complete();

        assertThat(ride.getStatus()).isEqualTo(RideStatus.COMPLETED);
    }

    @Test
    void complete_whenNotAccepted_shouldThrow() {
        Ride ride = Ride.builder().id(UUID.randomUUID()).userId(userId).origin(makeAddress("A")).destination(makeAddress("B")).build();

        assertThatThrownBy(ride::complete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não está ACCEPTED");
    }

    @Test
    void equalsAndHashCode_shouldBeBasedOnId() {
        Ride ride1 = Ride.builder().id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")).userId(userId).build();
        Ride ride2 = Ride.builder().id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")).userId(UUID.randomUUID()).build();
        Ride ride3 = Ride.builder().id(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")).userId(userId).build();

        assertThat(ride1).isEqualTo(ride2);
        assertThat(ride1).isNotEqualTo(ride3);
        assertThat(ride1.hashCode()).isEqualTo(ride2.hashCode());
    }

    @Test
    void toString_shouldNotContainOriginOrDestination() {
        Ride ride = Ride.create(userId, makeAddress("Av Paulista"), makeAddress("Av Rio Branco"));
        String str = ride.toString();
        assertThat(str).doesNotContain("Av Paulista");
        assertThat(str).doesNotContain("Av Rio Branco");
    }
}
