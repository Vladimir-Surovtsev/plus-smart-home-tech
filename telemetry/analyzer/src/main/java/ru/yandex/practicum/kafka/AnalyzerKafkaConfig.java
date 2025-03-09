package ru.yandex.practicum.kafka;

import java.util.Map;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@ConfigurationProperties("analyzer.kafka")
@Component
public class AnalyzerKafkaConfig {
    public ProducerConfig producer;
    public ConsumerConfig consumer;
    private Properties hubConsumerProperties;
    private Properties snapshotConsumerProperties;
    private Map<String, String> topics;
}