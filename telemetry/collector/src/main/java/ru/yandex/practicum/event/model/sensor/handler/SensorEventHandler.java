package ru.yandex.practicum.event.model.sensor.handler;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface SensorEventHandler {
    SensorEventProto.PayloadCase getPayloadCase();

    void handle(SensorEventProto event);
}