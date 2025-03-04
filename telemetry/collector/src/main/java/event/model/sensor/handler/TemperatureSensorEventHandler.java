package event.model.sensor.handler;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Component
public class TemperatureSensorEventHandler implements SensorEventHandler {
    @Override
    public SensorEventProto.PayloadCase getPayloadCase() {
        return SensorEventProto.PayloadCase.TEMPERATURE;
    }

    @Override
    public void handle(SensorEventProto event) {

    }
}