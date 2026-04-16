package com.rideflow.shared.exception;

import org.springframework.http.HttpStatus;

public class QueuePublishException extends RideFlowException {

    public QueuePublishException(String topic, Throwable cause) {
        super("Falha ao publicar evento no tópico: " + topic, HttpStatus.SERVICE_UNAVAILABLE, "QUEUE_PUBLISH_ERROR");
        initCause(cause);
    }
}
