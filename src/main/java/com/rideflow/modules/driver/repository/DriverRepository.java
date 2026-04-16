package com.rideflow.modules.driver.repository;

import com.rideflow.modules.driver.domain.Driver;
import com.rideflow.modules.driver.domain.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    List<Driver> findByStatus(DriverStatus status);
}
