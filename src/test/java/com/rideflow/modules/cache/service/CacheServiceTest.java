package com.rideflow.modules.cache.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CacheService cacheService;

    @Test
    @DisplayName("Deve salvar corrida no Redis com TTL")
    void saveRideInProgress_shouldStoreInRedisWithTtl() {
        UUID rideId = UUID.randomUUID();
        Map<String, Object> data = Map.of("status", "ACCEPTED");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService.saveRideInProgress(rideId, data);

        verify(valueOperations).set(eq("ride:in-progress:" + rideId), eq(data), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Deve retornar dados do Redis quando cache hit")
    void findRideInProgress_withCacheHit_shouldReturnFromRedis() {
        UUID rideId = UUID.randomUUID();
        Map<String, Object> cached = Map.of("status", "ACCEPTED");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("ride:in-progress:" + rideId)).thenReturn(cached);

        Optional<Object> result = cacheService.findRideInProgress(rideId);

        assertThat(result).isPresent().contains(cached);
    }

    @Test
    @DisplayName("Deve retornar empty quando cache miss")
    void findRideInProgress_withCacheMiss_shouldReturnEmpty() {
        UUID rideId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("ride:in-progress:" + rideId)).thenReturn(null);

        Optional<Object> result = cacheService.findRideInProgress(rideId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar empty e logar warn quando Redis indisponível")
    void findRideInProgress_withRedisError_shouldReturnEmptyAndLogWarn() {
        UUID rideId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Connection refused"));

        Optional<Object> result = cacheService.findRideInProgress(rideId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve remover chave do Redis")
    void evictRide_shouldRemoveKeyFromRedis() {
        UUID rideId = UUID.randomUUID();
        when(redisTemplate.delete("ride:in-progress:" + rideId)).thenReturn(true);

        cacheService.evictRide(rideId);

        verify(redisTemplate).delete("ride:in-progress:" + rideId);
    }
}
