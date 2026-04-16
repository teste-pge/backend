package com.rideflow.modules.notification.facade;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface NotificationFacade {

    SseEmitter connectDriver(UUID driverId);

    void disconnectDriver(UUID driverId);

    int getConnectedCount();
}
