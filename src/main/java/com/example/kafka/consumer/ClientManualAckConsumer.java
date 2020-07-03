package com.example.kafka.consumer;

import com.example.kafka.domain.Client;
import com.example.kafka.domain.exception.ClientConsumerAcknowledgmentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class ClientManualAckConsumer {

    private CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    @KafkaListener(
            topics = "${object.topics.client-ack}",
            groupId = "${object.group-ids.client-ack}",
            containerFactory = "clientManualAckKafkaListenerContainerFactory")
    public void process(@Payload Client data, Acknowledgment acknowledgment) {
        log.info("processing manual ack client data='{}'", data);

        if (!data.getId().equals("1")) {
            throw new ClientConsumerAcknowledgmentException("Client manual ack for id not of interest " + data.getId());
        }

        latch.countDown();
        log.info("processed manual ack client data='{}'", data);
        acknowledgment.acknowledge();
    }
}
