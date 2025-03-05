package ru.yandex.practicum.event.model.hub.handler;

import ru.yandex.practicum.event.kafka.KafkaClient;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.KafkaTopics;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    private final KafkaClient kafkaClient;

    @Override
    public HubEventProto.PayloadCase getPayloadCase() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        HubEventAvro hubEventAvro = new HubEventAvro(
                event.getHubId(),
                Instant.ofEpochSecond(event.getTimestamp().getSeconds()),
                new DeviceAddedEventAvro(
                        event.getDeviceAdded().getId(),
                        DeviceTypeAvro.valueOf(event.getDeviceAdded().getType().name())
                )
        );

        ProducerRecord<String, SpecificRecordBase> producerRecord = new ProducerRecord<>(
                KafkaTopics.HUBS,
                event.getHubId(),
                hubEventAvro
        );

        kafkaClient.getProducer().send(producerRecord);
    }
}