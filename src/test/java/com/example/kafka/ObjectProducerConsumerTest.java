package com.example.kafka;

import com.example.kafka.consumer.AccountConsumer;
import com.example.kafka.consumer.ClientConsumer;
import com.example.kafka.consumer.ClientManualAckConsumer;
import com.example.kafka.domain.Account;
import com.example.kafka.domain.Client;
import com.example.kafka.producer.ObjectProducer;
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
public class ObjectProducerConsumerTest {

  @Value("${object.topics.account}")
  private String ACCOUNT_TOPIC;

  @Value("${object.topics.client}")
  private String CLIENT_TOPIC;

  @Value("${object.topics.client-ack}")
  private String CLIENT_ACK_TOPIC;

  @Autowired
  private ObjectProducer producer;

  @Autowired
  private AccountConsumer accountConsumer;

  @Autowired
  private ClientConsumer clientConsumer;

  @Autowired
  private ClientManualAckConsumer clientManualAckConsumer;

  @Test
  public void whenAccountObjectTopicProducedItConsumes() throws Exception {
    producer.send(ACCOUNT_TOPIC, new Account("1234", "Bob"));

    accountConsumer.getLatch().await(1000, TimeUnit.MILLISECONDS);
    assertThat(accountConsumer.getLatch().getCount()).isEqualTo(0);
  }

  @Test
  public void whenClientObjectTopicProducedItConsumesAndAcknowledges() throws Exception {
    producer.send(CLIENT_TOPIC, new Client("1", "Foo", "Bar"));
    producer.send(CLIENT_ACK_TOPIC, new Client("1", "FooACK", "BarACK"));

    clientConsumer.getLatch().await(1000, TimeUnit.MILLISECONDS);
    clientManualAckConsumer.getLatch().await(1000, TimeUnit.MILLISECONDS);
    assertThat(clientConsumer.getLatch().getCount()).isEqualTo(0);
    assertThat(clientManualAckConsumer.getLatch().getCount()).isEqualTo(0);
  }

  @Test
  public void whenClientObjectTopicProducedItConsumerFailsAfterSeveralRetiresAndAcknowledges() throws Exception {
    producer.send(CLIENT_TOPIC, new Client("2", "Foo ", "Bar "));
    producer.send(CLIENT_ACK_TOPIC, new Client("2", "FooACK", "BarACK"));

    clientConsumer.getLatch().await(2000, TimeUnit.MILLISECONDS);
    clientManualAckConsumer.getLatch().await(2000, TimeUnit.MILLISECONDS);
    assertThat(clientConsumer.getLatch().getCount()).isEqualTo(1);
    assertThat(clientManualAckConsumer.getLatch().getCount()).isEqualTo(1);
  }
}
