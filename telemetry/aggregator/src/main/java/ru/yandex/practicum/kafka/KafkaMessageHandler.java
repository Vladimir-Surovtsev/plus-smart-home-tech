package ru.yandex.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.KafkaTopics;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KafkaMessageHandler {
    private final KafkaTemplate<String, SensorsSnapshotAvro> kafkaTemplate;
    private final Map<String, SensorsSnapshotAvro> hubIdToSensorsSnapshot = new HashMap<>();

    @KafkaListener(topics = KafkaTopics.SENSORS)
    public Optional<SensorsSnapshotAvro> handleSensorEvent(SensorEventAvro event) {
        if (hubIdToSensorsSnapshot.containsKey(event.getHubId())) {
            SensorsSnapshotAvro sensorsSnapshotAvro = hubIdToSensorsSnapshot.get(event.getHubId());
            if (sensorsSnapshotAvro.getSensorsState().containsKey(event.getId())) {
                SensorStateAvro oldState = sensorsSnapshotAvro.getSensorsState().get(event.getId());
                if (
                        oldState.getTimestamp().isAfter(event.getTimestamp())
                                || oldState.getData().equals(event.getPayload())
                ) {
                    return Optional.empty();
                }

                SensorStateAvro newState = SensorStateAvro.newBuilder()
                        .setTimestamp(event.getTimestamp())
                        .setData(event.getPayload())
                        .build();

                sensorsSnapshotAvro.getSensorsState().put(event.getId(), newState);
                kafkaTemplate.send(KafkaTopics.SNAPSHOTS, sensorsSnapshotAvro);
                return Optional.of(sensorsSnapshotAvro);
            }

            SensorStateAvro state = SensorStateAvro.newBuilder()
                    .setTimestamp(event.getTimestamp())
                    .setData(event.getPayload())
                    .build();
            sensorsSnapshotAvro.getSensorsState().put(event.getId(), state);
            kafkaTemplate.send(KafkaTopics.SNAPSHOTS, sensorsSnapshotAvro);
            return Optional.of(sensorsSnapshotAvro);
        }

        HashMap<String, SensorStateAvro> sensorsState = new HashMap<>();
        SensorStateAvro sensorStateAvro = SensorStateAvro.newBuilder().setTimestamp(event.getTimestamp()).setData(event.getPayload()).build();
        sensorsState.put(event.getId(), sensorStateAvro);
        SensorsSnapshotAvro sensorsSnapshotAvro = new SensorsSnapshotAvro(
                event.getHubId(),
                event.getTimestamp(),
                sensorsState
        );
        hubIdToSensorsSnapshot.put(event.getHubId(), sensorsSnapshotAvro);
        kafkaTemplate.send(KafkaTopics.SNAPSHOTS, sensorsSnapshotAvro);
        return Optional.of(sensorsSnapshotAvro);
    }
}