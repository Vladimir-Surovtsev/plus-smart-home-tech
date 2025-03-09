package ru.yandex.practicum.kafka.config;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties("aggregator.kafka")
public class KafkaConfig {
    public ProducerConfig producer;
    public ConsumerConfig consumer;
    private Map<String, String> topics;

    @Bean
    public KafkaProducer<String, SpecificRecordBase> kafkaProducer() {
        return new KafkaProducer<>(producer.properties);
    }

    @Bean
    public KafkaConsumer<String, SpecificRecordBase> kafkaConsumer() {
        return new KafkaConsumer<>(consumer.properties);
    }

    @Bean
    public EnumMap<TopicType, String> topics() {
        return new Topics(topics).getTopics();
    }

    public enum TopicType {
        TELEMETRY_SENSORS, TELEMETRY_SNAPSHOTS;

        public static TopicType from(String type) {
            switch (type) {
                case "telemetry-sensors" -> {
                    return TopicType.TELEMETRY_SENSORS;
                }
                case "telemetry-snapshots" -> {
                    return TopicType.TELEMETRY_SNAPSHOTS;
                }
                default -> throw new RuntimeException("Topic type not found");

            }
        }
    }

    @Getter
    public static class ProducerConfig {
        public final Properties properties;

        public ProducerConfig(Properties properties) {
            this.properties = properties;
        }
    }

    @Getter
    public static class ConsumerConfig {
        public final Properties properties;

        public ConsumerConfig(Properties properties) {
            this.properties = properties;
        }
    }

    @Getter
    public static class Topics {
        private final EnumMap<TopicType, String> topics = new EnumMap<>(TopicType.class);

        public Topics(Map<String, String> topics) {
            for (Map.Entry<String, String> entry : topics.entrySet()) {
                this.topics.put(TopicType.from(entry.getKey()), entry.getValue());
            }
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class KafkaEventProducer {
        private final KafkaProducer<String, SpecificRecordBase> kafkaProducer;
    }

    @Component
    @RequiredArgsConstructor
    public static class KafkaEventConsumer {
        private final KafkaConsumer<String, SpecificRecordBase> kafkaConsumer;
    }
}