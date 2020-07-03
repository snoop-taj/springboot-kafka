package com.example.kafka.domain.exception;

public class ClientConsumerAcknowledgmentException extends RuntimeException {

    public ClientConsumerAcknowledgmentException(String message) {
        super(message);
    }
}
