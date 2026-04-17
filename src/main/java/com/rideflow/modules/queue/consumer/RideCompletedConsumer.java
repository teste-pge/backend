package com.rideflow.modules.queue.consumer;

import com.rideflow.modules.notification.service.NotificationService;
import com.rideflow.modules.queue.dto.RideCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideCompletedConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "ride.completed",
            groupId = "ride-notification-group"
    )
    public void onRideCompleted(RideCompletedEvent event, Acknowledgment ack) {
        log.info("Evento ride.completed recebido do Kafka: rideId={}, driverId={}", event.rideId(), event.driverId());
        notificationService.notifyPassengerRideCompleted(event);
        ack.acknowledge();
    }
}
