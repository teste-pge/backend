package com.rideflow.modules.cache.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private static final String KEY_PREFIX = "ride:in-progress:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${rideflow.ride.cache-ttl-hours:4}")
    private long cacheTtlHours;

    public void saveRideInProgress(UUID rideId, Object rideData) {
        try {
            String key = buildKey(rideId);
            redisTemplate.opsForValue().set(key, rideData, cacheTtlHours, TimeUnit.HOURS);
            log.info("Cache gravado: key={}, TTL={}h", key, cacheTtlHours);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis indisponível para escrita. rideId={}. Erro: {}", rideId, ex.getMessage());
        } catch (Exception ex) {
            log.warn("Erro ao gravar no Redis. rideId={}. Erro: {}", rideId, ex.getMessage());
        }
    }

    public Optional<Object> findRideInProgress(UUID rideId) {
        try {
            Object cached = redisTemplate.opsForValue().get(buildKey(rideId));
            if (cached != null) {
                log.debug("Cache HIT: rideId={}", rideId);
            } else {
                log.debug("Cache MISS: rideId={}", rideId);
            }
            return Optional.ofNullable(cached);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis indisponível para leitura. Fallback para PostgreSQL. rideId={}", rideId);
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("Erro ao ler do Redis. Fallback para PostgreSQL. rideId={}. Erro: {}", rideId, ex.getMessage());
            return Optional.empty();
        }
    }

    public void evictRide(UUID rideId) {
        try {
            String key = buildKey(rideId);
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("Cache evicted: key={}", key);
            }
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis indisponível para eviction. rideId={}. Erro: {}", rideId, ex.getMessage());
        } catch (Exception ex) {
            log.warn("Erro ao remover do Redis. rideId={}. Erro: {}", rideId, ex.getMessage());
        }
    }

    private String buildKey(UUID rideId) {
        return KEY_PREFIX + rideId;
    }
}
