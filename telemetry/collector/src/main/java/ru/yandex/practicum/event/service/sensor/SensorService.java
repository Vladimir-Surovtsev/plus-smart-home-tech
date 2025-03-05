package ru.yandex.practicum.event.service.sensor;

import ru.yandex.practicum.event.model.sensor.SensorEvent;

public interface SensorService {
    void sendEventToKafka(SensorEvent sensorEvent);
}