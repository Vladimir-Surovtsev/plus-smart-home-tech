package ru.yandex.practicum.handler.hub;

import org.springframework.stereotype.Service;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.handler.BaseHubEventHandler;
import ru.yandex.practicum.kafka.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

@Service
public class DeviceAddedEventHandlerBase extends BaseHubEventHandler<DeviceAddedEventAvro> {
    public DeviceAddedEventHandlerBase(KafkaConfig.KafkaEventProducer producer, KafkaConfig kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    protected DeviceAddedEventAvro mapToAvro(HubEventProto event) {
        var deviceAddedEvent = event.getDeviceAdded();

        return new DeviceAddedEventAvro(
                deviceAddedEvent.getId(),
                DeviceTypeAvro.valueOf(deviceAddedEvent.getType().name())
        );
    }

    @Override
    public HubEventProto.PayloadCase getEventType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }
}