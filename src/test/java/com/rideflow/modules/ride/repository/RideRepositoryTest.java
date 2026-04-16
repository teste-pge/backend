package com.rideflow.modules.ride.repository;

import com.rideflow.modules.ride.domain.Address;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.domain.RideStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class RideRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("rideflow_test")
            .withUsername("rideflow")
            .withPassword("rideflow")
            .withInitScript("db/migration/V1__create_rides_table.sql");

    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    RideRepository rideRepository;

    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID DRIVER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private Address origin() {
        return new Address("01310-100", "Av. Paulista", "1000", null,
                "Bela Vista", "São Paulo", "SP");
    }

    private Address destination() {
        return new Address("20040-020", "Av. Rio Branco", "200", null,
                "Centro", "Rio de Janeiro", "RJ");
    }

    private Ride newRide() {
        Ride ride = new Ride();
        ride.setUserId(USER_ID);
        ride.setOrigin(origin());
        ride.setDestination(destination());
        return ride;
    }

    @BeforeEach
    void cleanUp() {
        rideRepository.deleteAll();
    }

    @Test
    void save_shouldPersistRideWithAllFields() {
        Ride saved = rideRepository.saveAndFlush(newRide());

        Ride found = rideRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getStatus()).isEqualTo(RideStatus.PENDING);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        assertThat(found.getOrigin().getCep()).isEqualTo("01310-100");
        assertThat(found.getDestination().getCidade()).isEqualTo("Rio de Janeiro");
    }

    @Test
    void findById_shouldReturnPersistedRide() {
        Ride saved = rideRepository.save(newRide());

        Optional<Ride> found = rideRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void findByStatus_shouldReturnOnlyMatchingRides() {
        rideRepository.save(newRide());
        rideRepository.save(newRide());

        Ride accepted = newRide();
        accepted.setDriverId(DRIVER_ID);
        accepted.setStatus(RideStatus.ACCEPTED);
        rideRepository.save(accepted);

        Page<Ride> pending = rideRepository.findByStatus(RideStatus.PENDING, PageRequest.of(0, 10));
        Page<Ride> acceptedPage = rideRepository.findByStatus(RideStatus.ACCEPTED, PageRequest.of(0, 10));

        assertThat(pending.getTotalElements()).isEqualTo(2);
        assertThat(acceptedPage.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByUserId_shouldReturnOnlyUserRides() {
        UUID otherUser = UUID.randomUUID();

        rideRepository.save(newRide());
        rideRepository.save(newRide());

        Ride otherRide = newRide();
        otherRide.setUserId(otherUser);
        rideRepository.save(otherRide);

        Page<Ride> userRides = rideRepository.findByUserId(USER_ID, PageRequest.of(0, 10));

        assertThat(userRides.getTotalElements()).isEqualTo(2);
        assertThat(userRides.getContent())
                .extracting(Ride::getUserId)
                .containsOnly(USER_ID);
    }

    @Test
    void findPendingRideForUpdate_shouldReturnRide_whenStatusIsPending() {
        Ride saved = rideRepository.save(newRide());

        Optional<Ride> found = rideRepository.findPendingRideForUpdate(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(RideStatus.PENDING);
    }

    @Test
    void findPendingRideForUpdate_shouldReturnEmpty_whenRideIsAlreadyAccepted() {
        Ride ride = newRide();
        ride.setDriverId(DRIVER_ID);
        ride.setStatus(RideStatus.ACCEPTED);
        Ride saved = rideRepository.save(ride);

        Optional<Ride> found = rideRepository.findPendingRideForUpdate(saved.getId());

        assertThat(found).isEmpty();
    }
}
