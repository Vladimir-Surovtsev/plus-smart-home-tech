package event.kafka;

import event.kafka.deserializer.GeneralAvroDeserializer;
import event.kafka.serializer.GeneralAvroSerializer;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaClientConfiguration {
    @Bean
    public KafkaClient getClient() {
        return new KafkaClient() {
            private Consumer<String, SpecificRecordBase> consumer;
            private Producer<String, SpecificRecordBase> producer;

            @Override
            public Producer<String, SpecificRecordBase> getProducer() {
                if (producer == null) {
                    Properties props = new Properties();
                    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
                    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);
                    producer = new KafkaProducer<>(props);
                }
                return producer;
            }

            @Override
            public Consumer<String, SpecificRecordBase> getConsumer() {
                if (consumer == null) {
                    Properties props = new Properties();
                    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
                    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GeneralAvroDeserializer.class);
                    props.put(ConsumerConfig.GROUP_ID_CONFIG, "collector");
                    consumer = new KafkaConsumer<>(props);
                }
                return consumer;
            }

            @Override
            public void stop() {
                if (consumer != null) {
                    consumer.close();
                }
                if (producer != null) {
                    producer.close();
                }
            }
        };
    }
}