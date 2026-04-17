package com.rideflow.modules.queue.producer;

import com.rideflow.modules.queue.dto.RideAcceptedEvent;
import com.rideflow.modules.queue.dto.RideCompletedEvent;
import com.rideflow.modules.queue.dto.RideCreatedEvent;
import com.rideflow.modules.queue.dto.RideRejectedEvent;
import com.rideflow.shared.exception.QueuePublishException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideEventProducer {

    public static final String TOPIC_RIDE_CREATED = "ride.created";
    public static final String TOPIC_RIDE_ACCEPTED = "ride.accepted";
    public static final String TOPIC_RIDE_REJECTED = "ride.rejected";
    public static final String TOPIC_RIDE_COMPLETED = "ride.completed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void publishRideCreated(RideCreatedEvent event) {
        sendSync(TOPIC_RIDE_CREATED, event.rideId().toString(), event);
        log.info("Evento ride.created publicado para rideId={}", event.rideId());
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void publishRideAccepted(RideAcceptedEvent event) {
        sendSync(TOPIC_RIDE_ACCEPTED, event.rideId().toString(), event);
        log.info("Evento ride.accepted publicado para rideId={}", event.rideId());
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void publishRideRejected(RideRejectedEvent event) {
        sendSync(TOPIC_RIDE_REJECTED, event.rideId().toString(), event);
        log.info("Evento ride.rejected publicado para rideId={}", event.rideId());
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void publishRideCompleted(RideCompletedEvent event) {
        sendSync(TOPIC_RIDE_COMPLETED, event.rideId().toString(), event);
        log.info("Evento ride.completed publicado para rideId={}", event.rideId());
    }

    @Recover
    public void recoverRideCompleted(Exception ex, RideCompletedEvent event) {
        log.error("Falha definitiva ao publicar ride.completed para rideId={} após 3 tentativas", event.rideId(), ex);
        throw new QueuePublishException(TOPIC_RIDE_COMPLETED, ex);
    }

    @Recover
    public void recoverRideCreated(Exception ex, RideCreatedEvent event) {
        log.error("Falha definitiva ao publicar ride.created para rideId={} após 3 tentativas", event.rideId(), ex);
        throw new QueuePublishException(TOPIC_RIDE_CREATED, ex);
    }

    @Recover
    public void recoverRideAccepted(Exception ex, RideAcceptedEvent event) {
        log.error("Falha definitiva ao publicar ride.accepted para rideId={} após 3 tentativas", event.rideId(), ex);
        throw new QueuePublishException(TOPIC_RIDE_ACCEPTED, ex);
    }

    @Recover
    public void recoverRideRejected(Exception ex, RideRejectedEvent event) {
        log.error("Falha definitiva ao publicar ride.rejected para rideId={} após 3 tentativas", event.rideId(), ex);
        throw new QueuePublishException(TOPIC_RIDE_REJECTED, ex);
    }

    private void sendSync(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, payload).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrompida ao publicar no tópico " + topic, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new RuntimeException("Falha ao publicar no tópico " + topic, e);
        }
    }
}
