package com.example.kafka.consumer;

import com.example.kafka.domain.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class AccountConsumer {

    private CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    @KafkaListener(
            topics = "${object.topics.account}",
            groupId = "${object.group-ids.account}",
            containerFactory = "accountKafkaListenerContainerFactory")
    public void receive(@Payload Account data) {
        log.info("received account data='{}'", data);
        latch.countDown();
    }
}
