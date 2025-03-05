package ru.yandex.practicum.service.handler.snapshot;

import org.apache.avro.specific.SpecificRecordBase;

public interface SnapshotStateHandler<T extends SpecificRecordBase> {
    String getType();

    void handle(String hubId, String sensorId, T payload);
}