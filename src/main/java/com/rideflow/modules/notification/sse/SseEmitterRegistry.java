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
    private final Map<UUID, SseEmitter> passengerEmitters = new ConcurrentHashMap<>();

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

    public SseEmitter registerPassenger(UUID userId) {
        SseEmitter existing = passengerEmitters.get(userId);
        if (existing != null) {
            existing.complete();
            passengerEmitters.remove(userId);
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            passengerEmitters.remove(userId);
            log.debug("SSE passenger emitter completado para userId={}", userId);
        });

        emitter.onTimeout(() -> {
            passengerEmitters.remove(userId);
            log.debug("SSE passenger emitter timeout para userId={}", userId);
        });

        emitter.onError(ex -> {
            passengerEmitters.remove(userId);
            log.warn("SSE passenger emitter erro para userId={}: {}", userId, ex.getMessage());
        });

        passengerEmitters.put(userId, emitter);
        log.info("Passageiro userId={} conectado via SSE. Total conexões passageiros: {}", userId, passengerEmitters.size());

        return emitter;
    }

    public void removePassenger(UUID userId) {
        SseEmitter emitter = passengerEmitters.remove(userId);
        if (emitter != null) {
            emitter.complete();
            log.info("Passageiro userId={} desconectado. Total conexões passageiros: {}", userId, passengerEmitters.size());
        }
    }

    public void sendToPassenger(UUID userId, String eventName, Object data) {
        SseEmitter emitter = passengerEmitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.warn("Falha ao enviar SSE para passageiro userId={}. Removendo emitter.", userId);
                passengerEmitters.remove(userId);
            }
        }
    }

    @Scheduled(fixedRate = 30000)
    public void heartbeat() {
        if (emitters.isEmpty() && passengerEmitters.isEmpty()) {
            return;
        }

        log.debug("Enviando heartbeat para {} motoristas e {} passageiros conectados", emitters.size(), passengerEmitters.size());
        Map<String, String> hb = Map.of("timestamp", java.time.Instant.now().toString());
        broadcast("HEARTBEAT", hb);
        passengerEmitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("HEARTBEAT").data(hb));
            } catch (IOException e) {
                log.warn("Falha ao enviar heartbeat para passageiro userId={}. Removendo.", userId);
                passengerEmitters.remove(userId);
            }
        });
    }

    public int getConnectedCount() {
        return emitters.size();
    }
}
