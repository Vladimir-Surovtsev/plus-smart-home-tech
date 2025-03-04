package event.model.sensor.handler;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Component
public class LightSensorEventHandler implements SensorEventHandler {
    @Override
    public SensorEventProto.PayloadCase getPayloadCase() {
        return SensorEventProto.PayloadCase.LIGHT;
    }

    @Override
    public void handle(SensorEventProto event) {

    }
}