package com.example.kafka.consumer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConsumerConfig {
    private static final String CONTEXT_ACKNOWLEDGMENT = "acknowledgment";
    private static final String CONTEXT_RECORD = "record";
    private final KafkaConsumerConfigProperties configProperties;

    public KafkaConsumerConfig(KafkaConsumerConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    // String
    @Bean(name = "stringKafkaListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>>
    stringKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(stringConsumerConfigs()));

        return factory;
    }

    // Account
    @Bean(name = "accountKafkaListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>>
    accountKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(objectConsumerConfigs()));

        return factory;
    }

    // Client
    @Bean(name = "clientKafkaListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>>
    clientKafkaListenerContainerFactory(@Qualifier("objectKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(objectConsumerConfigs()));
        factory.setErrorHandler(new SeekToCurrentErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(
                        Long.parseLong(configProperties.getObject().get("client.retry-interval")),
                        Long.parseLong(configProperties.getObject().get("client.retry-attempts"))
                )
        ));

        return factory;
    }

    // Client Manual Ack
    @Bean(name = "clientManualAckKafkaListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>>
    clientManualAckKafkaListenerContainerFactory(
            @Qualifier("objectKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate,
            @Qualifier("manualAckRetryTemplate") RetryTemplate retryTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(objectManualAckConsumerConfigs()));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setRetryTemplate(retryTemplate);
        factory.setRecoveryCallback(context -> {
            Acknowledgment acknowledgment = (Acknowledgment) context.getAttribute(CONTEXT_ACKNOWLEDGMENT);
            ConsumerRecord record = (ConsumerRecord) context.getAttribute(CONTEXT_RECORD);
            String errorTopic = record.topic() + ".ERROR";

            log.error(
                    "Retry limit has been exceeded! Retry exhausted after {} times for topic {} with record {}.",
                    context.getRetryCount(),
                    record.topic(),
                    record.value()
            );

            kafkaTemplate.send(errorTopic, record.value());

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

            return null;
        });

        return factory;
    }

    // Retry Template
    @Bean("manualAckRetryTemplate")
    public RetryTemplate retryTemplate() {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(Integer.parseInt(configProperties.getObject().get("client.retry-attempts")));

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(Long.parseLong(configProperties.getObject().get("client.retry-interval")));

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
//        retryTemplate.setListeners(new KafkaRetryListener[]{kafkaRetryListener()});

        return retryTemplate;
    }

    private Map<String, Object> stringConsumerConfigs() {
        String bootstrapServer = configProperties.getString().get("bootstrap-servers");
        log.info("Boot Strap Server for string - {}", bootstrapServer);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, configProperties.getString().get("key-deserializer"));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, configProperties.getString().get("value-deserializer"));
        return props;
    }

    private Map<String, Object> objectConsumerConfigs() {
        String bootstrapServer = configProperties.getObject().get("bootstrap-servers");
        log.info("Boot Strap Server for object - {}", bootstrapServer);

        Map<String, Object> props = new HashMap<>();
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, configProperties.getObject().get("key-deserializer"));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, configProperties.getObject().get("value-deserializer"));
        return props;
    }

    private Map<String, Object> objectManualAckConsumerConfigs() {
        String bootstrapServer = configProperties.getObject().get("bootstrap-servers");
        log.info("Boot Strap Server for object - {}", bootstrapServer);

        Map<String, Object> props = new HashMap<>();
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, configProperties.getObject().get("key-deserializer"));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, configProperties.getObject().get("value-deserializer"));
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, configProperties.getObject().get("client.auto-commit"));

        return props;
    }
}
