package com.rideflow.modules.notification.controller;

import com.rideflow.modules.notification.sse.SseEmitterRegistry;
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
public class NotificationController {

    private final SseEmitterRegistry emitterRegistry;

    @GetMapping(value = "/drivers/{driverId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
}
