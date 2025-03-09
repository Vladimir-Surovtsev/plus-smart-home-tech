package ru.yandex.practicum.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.KafkaConfig;
import ru.yandex.practicum.kafka.TopicType;

@Slf4j
@AllArgsConstructor
public abstract class BaseHubEventHandler<T extends SpecificRecordBase> implements HubEventHandler {
    protected final KafkaConfig.KafkaEventProducer producer;
    protected final KafkaConfig kafkaConfig;

    protected abstract T mapToAvro(HubEventProto event);

    public void handle(HubEventProto event) {
        T avroEvent = mapToAvro(event);
        var topic = kafkaConfig.getProducer().getTopics().get(getTopicType());
        log.debug("Publish hub event {} to topic {}", getEventType(), topic);
        producer.send(topic, event.getHubId(), avroEvent);
    }

    public TopicType getTopicType() {
        return TopicType.HUBS_EVENTS;
    }

    public abstract HubEventProto.PayloadCase getEventType();
}