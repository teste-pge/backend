package com.rideflow.modules.queue.consumer;

import com.rideflow.modules.notification.service.NotificationService;
import com.rideflow.modules.queue.dto.RideCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideCreatedConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "ride.created",
            groupId = "ride-notification-group"
    )
    public void onRideCreated(RideCreatedEvent event, Acknowledgment ack) {
        log.info("Evento ride.created recebido do Kafka: rideId={}", event.rideId());
        notificationService.notifyAllDrivers(event);
        ack.acknowledge();
    }
}
