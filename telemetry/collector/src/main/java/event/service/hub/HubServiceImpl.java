package event.service.hub;

import event.kafka.KafkaClient;
import event.kafka.KafkaTopics;
import event.model.hub.DeviceAddedEvent;
import event.model.hub.DeviceRemovedEvent;
import event.model.hub.HubEvent;
import event.model.hub.ScenarioAddedEvent;
import event.model.hub.ScenarioRemovedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.ScenarioRemovedEventAvro;

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
                payload = new ScenarioRemovedEventAvro(scenarioRemovedEvent.getName());
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