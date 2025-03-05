package ru.yandex.practicum.event.service.hub;

import ru.yandex.practicum.event.kafka.KafkaClient;
import ru.yandex.practicum.event.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.event.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.event.model.hub.HubEvent;
import ru.yandex.practicum.event.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.event.model.hub.ScenarioRemovedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.KafkaTopics;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Service
@RequiredArgsConstructor
public class HubServiceImpl implements HubService {
    private final KafkaClient kafkaClient;

    @Override
    public void sendEventToKafka(HubEvent hubEvent) {
        Object payload;
        switch (hubEvent.getType()) {
            case DEVICE_ADDED -> {
                DeviceAddedEvent deviceAddedEvent = (DeviceAddedEvent) hubEvent;
                payload = new DeviceAddedEventAvro(deviceAddedEvent.getId(), deviceAddedEvent.getDeviceType());
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEvent deviceRemovedEvent = (DeviceRemovedEvent) hubEvent;
                payload = new DeviceRemovedEventAvro(deviceRemovedEvent.getId());
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEvent scenarioAddedEvent = (ScenarioAddedEvent) hubEvent;
                payload = new ScenarioAddedEventAvro(scenarioAddedEvent.getName(), scenarioAddedEvent.getConditions(), scenarioAddedEvent.getActions());
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEvent scenarioRemovedEvent = (ScenarioRemovedEvent) hubEvent;
                payload = new ScenarioRemovedEventAvro(scenarioRemovedEvent.getId());
            }
            case null, default -> throw new IllegalStateException("Unexpected value: " + hubEvent.getType());
        }
        HubEventAvro hubEventAvro = new HubEventAvro(hubEvent.getHubId(), hubEvent.getTimestamp(), payload);
        ProducerRecord<String, SpecificRecordBase> producerRecord = new ProducerRecord<>(
                KafkaTopics.HUBS,
                hubEvent.getHubId(),
                hubEventAvro
        );
        kafkaClient.getProducer().send(producerRecord);
    }
}