package com.rideflow.modules.cache.facade;

import java.util.Optional;
import java.util.UUID;

public interface CacheFacade {

    void saveRideInProgress(UUID rideId, Object rideData);

    Optional<Object> findRideInProgress(UUID rideId);

    void evictRide(UUID rideId);
}
