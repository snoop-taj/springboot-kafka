package com.example.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class StringConsumer {

    private CountDownLatch latch;

    public CountDownLatch getLatch() {
        return latch;
    }

    @KafkaListener(
            topics = "${string.topic}",
            groupId = "${string.group-id}",
            containerFactory = "stringKafkaListenerContainerFactory")
    public void receive(@Payload String data) {
        log.info("received data='{}'", data);
        latch.countDown();
    }

    public void initiateCountDown(int count) {
        latch = new CountDownLatch(count);
    }
}
