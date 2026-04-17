package com.rideflow.modules.notification.controller;

import com.rideflow.modules.notification.sse.SseEmitterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Stream SSE de notificações em tempo real")
public class NotificationController {

    private final SseEmitterRegistry emitterRegistry;

    @GetMapping(value = "/drivers/{driverId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream de notificações do motorista",
            description = "Abre conexão SSE para receber notificações em tempo real: NEW_RIDE (broadcast), RIDE_ACCEPTED (específico)")
    public SseEmitter streamDriverNotifications(@PathVariable UUID driverId) {
        SseEmitter emitter = emitterRegistry.register(driverId);

        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data(Map.of(
                            "message", "Conexão estabelecida",
                            "driverId", driverId.toString(),
                            "timestamp", Instant.now().toString()
                    )));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @GetMapping(value = "/passengers/{userId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream de notificações do passageiro",
            description = "Abre conexão SSE para receber notificações de status da corrida: RIDE_ACCEPTED, RIDE_COMPLETED")
    public SseEmitter streamPassengerNotifications(@PathVariable UUID userId) {
        SseEmitter emitter = emitterRegistry.registerPassenger(userId);

        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data(Map.of(
                            "message", "Conexão estabelecida",
                            "userId", userId.toString(),
                            "timestamp", Instant.now().toString()
                    )));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
}
