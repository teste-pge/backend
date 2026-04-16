package com.rideflow.modules.queue.consumer;

import com.rideflow.modules.notification.service.NotificationService;
import com.rideflow.modules.queue.dto.RideAcceptedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideAcceptedConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "ride.accepted",
            groupId = "ride-notification-group"
    )
    public void onRideAccepted(RideAcceptedEvent event) {
        log.info("Evento ride.accepted recebido do Kafka: rideId={}, driverId={}", event.rideId(), event.driverId());
        notificationService.notifyRideAccepted(event);
    }
}
