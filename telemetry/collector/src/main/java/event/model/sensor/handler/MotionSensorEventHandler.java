package event.model.sensor.handler;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Component
public class MotionSensorEventHandler implements SensorEventHandler {
    @Override
    public SensorEventProto.PayloadCase getPayloadCase() {
        return SensorEventProto.PayloadCase.MOTION;
    }

    @Override
    public void handle(SensorEventProto event) {

    }
}