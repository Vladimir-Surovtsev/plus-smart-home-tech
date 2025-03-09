package ru.yandex.practicum.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.KafkaConfig;
import ru.yandex.practicum.kafka.TopicType;

@Slf4j
@AllArgsConstructor
public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {
    protected final KafkaConfig.KafkaEventProducer producer;
    protected final KafkaConfig kafkaConfig;

    protected abstract T mapToAvro(SensorEventProto event);

    public void handle(SensorEventProto event) {
        T avroEvent = mapToAvro(event);
        var topic = kafkaConfig.getProducer().getTopics().get(getTopicType());
        log.debug("Publish sensor event {} to topic {}", getEventType(), topic);
        producer.send(topic, event.getId(), avroEvent);
    }

    public TopicType getTopicType() {
        return TopicType.SENSORS_EVENTS;
    }

    public abstract SensorEventProto.PayloadCase getEventType();
}