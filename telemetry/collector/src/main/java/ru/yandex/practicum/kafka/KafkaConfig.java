package ru.yandex.practicum.kafka;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Configuration
@ConfigurationProperties("collector.kafka")
public class KafkaConfig {
    public ProducerConfig producer;

    @Bean
    public KafkaProducer<String, SpecificRecordBase> kafkaProducer() {
        return new KafkaProducer<>(producer.properties);
    }

    @Bean
    public EnumMap<TopicType, String> topics() {
        return producer.topics;
    }

    @Getter
    public static class ProducerConfig {
        public final Properties properties;
        private final EnumMap<TopicType, String> topics = new EnumMap<>(TopicType.class);

        public ProducerConfig(Properties properties, Map<String, String> topics) {
            this.properties = properties;
            topics.forEach((key, value) -> this.topics.put(TopicType.from(key), value));
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class KafkaEventProducer {
        private final KafkaProducer<String, SpecificRecordBase> producer;

        public <T extends SpecificRecordBase> void send(String topic, String key, T event) {
            var record = new ProducerRecord<String, SpecificRecordBase>(topic, key, event);
            producer.send(record);
        }
    }
}