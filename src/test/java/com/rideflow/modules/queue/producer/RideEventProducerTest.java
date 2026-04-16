package com.rideflow.modules.queue.producer;

import com.rideflow.modules.queue.dto.RideAcceptedEvent;
import com.rideflow.modules.queue.dto.RideCreatedEvent;
import com.rideflow.modules.queue.dto.RideRejectedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private RideEventProducer rideEventProducer;

    private static final UUID RIDE_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID DRIVER_ID = UUID.randomUUID();

    @SuppressWarnings("unchecked")
    private void mockKafkaSuccess() {
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(any(String.class), any(String.class), any())).thenReturn(future);
    }

    @Test
    @DisplayName("Deve publicar evento ride.created no Kafka")
    void publishRideCreated_shouldSendToKafka() {
        mockKafkaSuccess();
        RideCreatedEvent event = new RideCreatedEvent(
                RIDE_ID, USER_ID, "Rua A, 100 - Centro", "Rua B, 200 - Bairro", "PENDING", Instant.now());

        rideEventProducer.publishRideCreated(event);

        verify(kafkaTemplate).send(eq("ride.created"), eq(RIDE_ID.toString()), eq(event));
    }

    @Test
    @DisplayName("Deve publicar evento ride.accepted no Kafka")
    void publishRideAccepted_shouldSendToKafka() {
        mockKafkaSuccess();
        RideAcceptedEvent event = new RideAcceptedEvent(RIDE_ID, DRIVER_ID, Instant.now());

        rideEventProducer.publishRideAccepted(event);

        verify(kafkaTemplate).send(eq("ride.accepted"), eq(RIDE_ID.toString()), eq(event));
    }

    @Test
    @DisplayName("Deve publicar evento ride.rejected no Kafka")
    void publishRideRejected_shouldSendToKafka() {
        mockKafkaSuccess();
        RideRejectedEvent event = new RideRejectedEvent(RIDE_ID, DRIVER_ID, Instant.now());

        rideEventProducer.publishRideRejected(event);

        verify(kafkaTemplate).send(eq("ride.rejected"), eq(RIDE_ID.toString()), eq(event));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando Kafka falha")
    void publishRideCreated_onKafkaFailure_shouldThrow() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka down"));
        when(kafkaTemplate.send(any(String.class), any(String.class), any())).thenReturn(future);

        RideCreatedEvent event = new RideCreatedEvent(
                RIDE_ID, USER_ID, "Rua A, 100", "Rua B, 200", "PENDING", Instant.now());

        assertThatThrownBy(() -> rideEventProducer.publishRideCreated(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ride.created");
    }

    @Test
    @DisplayName("Deve lançar QueuePublishException no recover após retries exauridos")
    void recoverRideCreated_shouldThrowQueuePublishException() {
        RideCreatedEvent event = new RideCreatedEvent(
                RIDE_ID, USER_ID, "Rua A, 100", "Rua B, 200", "PENDING", Instant.now());

        assertThatThrownBy(() -> rideEventProducer.recoverRideCreated(
                new RuntimeException("Kafka down"), event))
                .isInstanceOf(com.rideflow.shared.exception.QueuePublishException.class);
    }
}
