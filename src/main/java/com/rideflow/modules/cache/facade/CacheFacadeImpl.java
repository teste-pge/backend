package com.rideflow.modules.cache.facade;

import com.rideflow.modules.cache.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CacheFacadeImpl implements CacheFacade {

    private final CacheService cacheService;

    @Override
    public void saveRideInProgress(UUID rideId, Object rideData) {
        cacheService.saveRideInProgress(rideId, rideData);
    }

    @Override
    public Optional<Object> findRideInProgress(UUID rideId) {
        return cacheService.findRideInProgress(rideId);
    }

    @Override
    public void evictRide(UUID rideId) {
        cacheService.evictRide(rideId);
    }
}
