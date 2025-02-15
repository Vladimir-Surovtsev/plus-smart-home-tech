package event.service.sensor;

import event.model.sensor.SensorEvent;

public interface SensorService {
    void sendEventToKafka(SensorEvent sensorEvent);
}