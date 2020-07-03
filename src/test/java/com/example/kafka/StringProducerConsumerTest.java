package com.example.kafka;

import com.example.kafka.consumer.StringConsumer;
import com.example.kafka.producer.StringProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StringProducerConsumerTest {

  @Value("${string.topic}")
  private String SIMPLE_TOPIC;

  @Autowired
  private StringProducer producer;

  @Autowired
  private StringConsumer consumer;

  @Test
  public void whenStringTopicProducedItConsumes() throws Exception {
    consumer.initiateCountDown(1);

    producer.send(SIMPLE_TOPIC, "Hello Spring Boot!");

    consumer.getLatch().await(1000, TimeUnit.MILLISECONDS);
    assertThat(consumer.getLatch().getCount()).isEqualTo(0);
  }

  @Test
  public void whenMultipleStringTopicProducedItConsumes() throws Exception {
    consumer.initiateCountDown(5);

    for (int i=0; i < 5; i++) {
      producer.send(SIMPLE_TOPIC, "Hello Spring Boot " + i);
    }

    consumer.getLatch().await(5000, TimeUnit.MILLISECONDS);
    assertThat(consumer.getLatch().getCount()).isEqualTo(0);
  }
}
