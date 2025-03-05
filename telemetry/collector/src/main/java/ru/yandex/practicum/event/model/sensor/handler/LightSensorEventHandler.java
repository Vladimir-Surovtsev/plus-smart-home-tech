package ru.yandex.practicum.event.model.sensor.handler;

import ru.yandex.practicum.event.kafka.KafkaClient;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.KafkaTopics;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class LightSensorEventHandler implements SensorEventHandler {
    private final KafkaClient kafkaClient;

    @Override
    public SensorEventProto.PayloadCase getPayloadCase() {
        return SensorEventProto.PayloadCase.LIGHT;
    }

    @Override
    public void handle(SensorEventProto event) {
        SensorEventAvro sensorEventAvro = SensorEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setId(event.getId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds()))
                .setPayload(new LightSensorAvro(
                        event.getLight().getLinkQuality(),
                        event.getLight().getLuminosity()
                ))
                .build();

        ProducerRecord<String, SpecificRecordBase> producerRecord = new ProducerRecord<>(KafkaTopics.SENSORS, sensorEventAvro.getHubId(), sensorEventAvro);
        kafkaClient.getProducer().send(producerRecord);
    }
}