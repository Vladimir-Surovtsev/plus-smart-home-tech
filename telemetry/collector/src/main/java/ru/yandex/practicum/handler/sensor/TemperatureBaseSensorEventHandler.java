package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Service;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.handler.BaseSensorEventHandler;
import ru.yandex.practicum.kafka.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Service
public class TemperatureBaseSensorEventHandler extends BaseSensorEventHandler<TemperatureSensorAvro> {
    public TemperatureBaseSensorEventHandler(KafkaConfig.KafkaEventProducer producer, KafkaConfig kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    protected TemperatureSensorAvro mapToAvro(SensorEventProto event) {
        var tempEvent = event.getTemperatureSensorEvent();

        return new TemperatureSensorAvro(
                event.getId(),
                event.getHubId(),
                event.getTimestamp().getSeconds(),
                tempEvent.getTemperatureC(),
                tempEvent.getTemperatureF()
        );
    }

    @Override
    public SensorEventProto.PayloadCase getEventType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR_EVENT;
    }
}