package com.rideflow.modules.notification.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitterRegistry {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter register(UUID driverId) {
        SseEmitter existing = emitters.get(driverId);
        if (existing != null) {
            existing.complete();
            emitters.remove(driverId);
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            emitters.remove(driverId);
            log.debug("SSE emitter completado para driverId={}", driverId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(driverId);
            log.debug("SSE emitter timeout para driverId={}", driverId);
        });

        emitter.onError(ex -> {
            emitters.remove(driverId);
            log.warn("SSE emitter erro para driverId={}: {}", driverId, ex.getMessage());
        });

        emitters.put(driverId, emitter);
        log.info("Motorista driverId={} conectado via SSE. Total conexões: {}", driverId, emitters.size());

        return emitter;
    }

    public void remove(UUID driverId) {
        SseEmitter emitter = emitters.remove(driverId);
        if (emitter != null) {
            emitter.complete();
            log.info("Motorista driverId={} desconectado. Total conexões: {}", driverId, emitters.size());
        }
    }

    public void broadcast(String eventName, Object data) {
        emitters.forEach((driverId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.warn("Falha ao enviar SSE para driverId={}. Removendo emitter.", driverId);
                emitters.remove(driverId);
            }
        });
    }

    public void sendToDriver(UUID driverId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(driverId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.warn("Falha ao enviar SSE para driverId={}. Removendo emitter.", driverId);
                emitters.remove(driverId);
            }
        }
    }

    @Scheduled(fixedRate = 30000)
    public void heartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

        log.debug("Enviando heartbeat para {} motoristas conectados", emitters.size());
        broadcast("HEARTBEAT", Map.of("timestamp", java.time.Instant.now().toString()));
    }

    public int getConnectedCount() {
        return emitters.size();
    }
}
