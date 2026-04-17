package com.rideflow.modules.ride.repository;

import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.domain.RideStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RideRepository extends JpaRepository<Ride, UUID> {

    Page<Ride> findByStatus(RideStatus status, Pageable pageable);

    Page<Ride> findByUserId(UUID userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.id = :id AND r.status = 'PENDING'")
    Optional<Ride> findPendingRideForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.id = :id AND r.driverId = :driverId AND r.status = 'ACCEPTED'")
    Optional<Ride> findAcceptedRideForDriver(@Param("id") UUID id, @Param("driverId") UUID driverId);

    @Query("SELECT r FROM Ride r WHERE r.userId = :userId AND r.status IN ('PENDING', 'ACCEPTED') ORDER BY r.createdAt DESC")
    Optional<Ride> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT r FROM Ride r WHERE r.driverId = :driverId AND r.status = 'ACCEPTED' ORDER BY r.acceptedAt DESC")
    Optional<Ride> findActiveByDriverId(@Param("driverId") UUID driverId);
}
