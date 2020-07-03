package com.example.kafka.consumer;

import com.example.kafka.domain.Client;
import com.example.kafka.domain.exception.ClientConsumerAcknowledgmentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class ClientConsumer {

    private CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    @KafkaListener(
            topics = "${object.topics.client}",
            groupId = "${object.group-ids.client}",
            containerFactory = "clientKafkaListenerContainerFactory")
    public void process(@Payload Client data, Consumer<String, Object> record) {
        log.info("processing client data='{}'", data);

        if (!data.getId().equals("1")) {
            record.commitSync();
            throw new ClientConsumerAcknowledgmentException("Client id not of interest " + data.getId());
        }

        log.info("processed client data='{}'", data);
        latch.countDown();
    }
}
