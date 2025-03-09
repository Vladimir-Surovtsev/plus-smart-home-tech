package ru.yandex.kafka;

import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@SuppressWarnings("ALL")
public class SensorSnapshotDeserializer extends BaseAvroDeserializer<SensorsSnapshotAvro> {
    public SensorSnapshotDeserializer() {
        super(SensorsSnapshotAvro.getClassSchema());
    }
}