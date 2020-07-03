package com.example.kafka.producer;

import com.example.kafka.domain.Account;
import com.example.kafka.domain.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ObjectProducer {

    @Qualifier("objectKafkaTemplate")
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ObjectProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, Account data) {
        log.info("sending account data='{}' to topic='{}'", data, topic);
        kafkaTemplate.send(topic, data);
    }

    public void send(String topic, Client data) {
        log.info("sending client data='{}' to topic='{}'", data, topic);
        kafkaTemplate.send(topic, data);
    }
}
