package ru.yandex.practicum.handler.hub;

import org.springframework.stereotype.Service;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.handler.BaseHubEventHandler;
import ru.yandex.practicum.kafka.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;

@Service
public class DeviceRemovedEventHandlerBase extends BaseHubEventHandler<DeviceRemovedEventAvro> {
    public DeviceRemovedEventHandlerBase(KafkaConfig.KafkaEventProducer producer, KafkaConfig kafkaTopics) {
        super(producer, kafkaTopics);
    }

    @Override
    protected DeviceRemovedEventAvro mapToAvro(HubEventProto event) {
        var deviceRemovedEvent = event.getDeviceRemoved();
        return new DeviceRemovedEventAvro(deviceRemovedEvent.getId());
    }

    @Override
    public HubEventProto.PayloadCase getEventType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }
}