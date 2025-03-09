package ru.yandex.practicum;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.kafka.config.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();
    private final EnumMap<KafkaConfig.TopicType, String> topics;


    public void start() {
        final String telemetrySensors = topics.get(KafkaConfig.TopicType.TELEMETRY_SENSORS);
        final String telemetrySnapshots = topics.get(KafkaConfig.TopicType.TELEMETRY_SNAPSHOTS);

        try {
            consumer.subscribe(Collections.singletonList(telemetrySensors));
            log.info("subscribe -> topic: {}", telemetrySensors);

            //noinspection InfiniteLoopStatement
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();

                    updateState(event).ifPresent(snapshot -> {
                        try {
                            producer.send(new ProducerRecord<>(telemetrySnapshots, snapshot.getHubId(), snapshot),
                                    (metadata, exception) -> {
                                    });
                            log.info("Snapshot hubId {} -> topic {}", snapshot.getHubId(), telemetrySnapshots);
                        } catch (Exception e) {
                            log.error("Ошибка при отправке слепка в топик", e);
                        }
                    });
                }

                try {
                    consumer.commitSync();
                } catch (Exception e) {
                    log.error("commitSync error ", e);
                }
            }
        } catch (Exception e) {
            log.error("sensor event error ", e);
        } finally {
            try {
                producer.flush();
                producer.close();
                consumer.close();
            } catch (Exception e) {
                log.error("resources close error ", e);
            }
        }
    }


    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        SensorsSnapshotAvro snapshot = snapshots.getOrDefault(event.getHubId(),
                SensorsSnapshotAvro.newBuilder()
                        .setHubId(event.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(System.currentTimeMillis()))
                        .setSensorsState(new HashMap<>())
                        .build());

        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());
        if (oldState != null
                && !oldState.getTimestamp().isBefore(Instant.ofEpochSecond(event.getTimestamp()))
                && oldState.getData().equals(event.getPayload())) {
            return Optional.empty();
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp()))
                .setData(event.getPayload())
                .build();

        snapshot.getSensorsState().put(event.getId(), newState);
        snapshot.setTimestamp(Instant.ofEpochSecond(event.getTimestamp()));
        snapshots.put(event.getHubId(), snapshot);

        return Optional.of(snapshot);
    }
}