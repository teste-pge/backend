package com.rideflow.modules.notification.facade;

import com.rideflow.modules.notification.sse.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationFacadeImpl implements NotificationFacade {

    private final SseEmitterRegistry emitterRegistry;

    @Override
    public SseEmitter connectDriver(UUID driverId) {
        return emitterRegistry.register(driverId);
    }

    @Override
    public void disconnectDriver(UUID driverId) {
        emitterRegistry.remove(driverId);
    }

    @Override
    public int getConnectedCount() {
        return emitterRegistry.getConnectedCount();
    }
}
