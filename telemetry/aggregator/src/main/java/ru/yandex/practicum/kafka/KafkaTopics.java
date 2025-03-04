package ru.yandex.practicum.kafka;

public class KafkaTopics {
    public static final String SNAPSHOTS = "telemetry.snapshots.v1";
    public static final String SENSORS = "telemetry.sensors.v1";

    private KafkaTopics() {
        throw new UnsupportedOperationException();
    }
}