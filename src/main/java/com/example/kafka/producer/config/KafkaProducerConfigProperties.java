package com.example.kafka.producer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties("kafka.config.producer")
public class KafkaProducerConfigProperties {
    private Map<String, String> string;
    private Map<String, String> object;
}
