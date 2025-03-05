package ru.yandex.practicum.event.model.hub.handler;

import ru.yandex.practicum.event.kafka.KafkaClient;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.KafkaTopics;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler {
    private final KafkaClient kafkaClient;

    @Override
    public HubEventProto.PayloadCase getPayloadCase() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        HubEventAvro hubEventAvro = new HubEventAvro(
                event.getHubId(),
                Instant.ofEpochSecond(event.getTimestamp().getSeconds()),
                new DeviceRemovedEventAvro(event.getDeviceRemoved().getId())
        );

        ProducerRecord<String, SpecificRecordBase> producerRecord = new ProducerRecord<>(
                KafkaTopics.HUBS,
                event.getHubId(),
                hubEventAvro
        );

        kafkaClient.getProducer().send(producerRecord);
    }
}