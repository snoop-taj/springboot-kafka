package com.example.kafka.producer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaProducerConfig {
    private final KafkaProducerConfigProperties configProperties;

    public KafkaProducerConfig(KafkaProducerConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    // String
    @Bean(name = "stringKafkaTemplate")
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(stringProducerConfigs()));
    }

    // Object
    @Bean(name = "objectKafkaTemplate")
    public KafkaTemplate<String, Object> objectKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(objectProducerConfigs()));
    }

    private Map<String, Object> stringProducerConfigs() {
        String bootstrapServer = configProperties.getString().get("bootstrap-servers");
        log.info("Boot Strap Server for producer string - {}", bootstrapServer);

        Map<String, Object> props = new HashMap<>();

        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, configProperties.getString().get("key-serializer"));
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, configProperties.getString().get("value-serializer"));

        return props;
    }

    private Map<String, Object> objectProducerConfigs() {
        String bootstrapServer = configProperties.getObject().get("bootstrap-servers");
        log.info("Boot Strap Server for producer object - {}", bootstrapServer);

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, configProperties.getObject().get("key-serializer"));
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, configProperties.getObject().get("value-serializer"));

        return props;
    }
}
