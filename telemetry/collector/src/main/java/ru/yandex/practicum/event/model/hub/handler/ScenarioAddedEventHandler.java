package ru.yandex.practicum.event.model.hub.handler;

import ru.yandex.practicum.event.kafka.KafkaClient;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.KafkaTopics;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    private final KafkaClient kafkaClient;

    @Override
    public HubEventProto.PayloadCase getPayloadCase() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        List<ScenarioConditionAvro> scenarioConditionAvroList = event.getScenarioAdded().getConditionsList().stream()
                .map(scenarioConditionProto -> new ScenarioConditionAvro(
                        scenarioConditionProto.getSensorId(),
                        ConditionTypeAvro.valueOf(scenarioConditionProto.getType().name()),
                        ConditionOperationAvro.valueOf(scenarioConditionProto.getOperation().name()),
                        scenarioConditionProto.getValueCase() == ScenarioConditionProto.ValueCase.BOOL_VALUE ? scenarioConditionProto.getBoolValue() : scenarioConditionProto.getIntValue()
                )).toList();

        List<DeviceActionAvro> deviceActionAvroList = event.getScenarioAdded().getActionsList().stream()
                .map(deviceActionProto -> new DeviceActionAvro(
                        deviceActionProto.getSensorId(),
                        ActionTypeAvro.valueOf(deviceActionProto.getType().name()),
                        deviceActionProto.getValue()
                ))
                .toList();

        ScenarioAddedEventAvro payload = new ScenarioAddedEventAvro(
                event.getScenarioAdded().getName(),
                scenarioConditionAvroList,
                deviceActionAvroList
        );

        HubEventAvro hubEventAvro = new HubEventAvro(
                event.getHubId(),
                Instant.ofEpochSecond(event.getTimestamp().getSeconds()),
                payload
        );

        ProducerRecord<String, SpecificRecordBase> producerRecord = new ProducerRecord<>(
                KafkaTopics.HUBS,
                event.getHubId(),
                hubEventAvro
        );

        kafkaClient.getProducer().send(producerRecord);
    }
}