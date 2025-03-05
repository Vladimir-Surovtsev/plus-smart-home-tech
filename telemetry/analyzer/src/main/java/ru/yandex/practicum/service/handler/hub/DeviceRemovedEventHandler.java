package ru.yandex.practicum.service.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.repository.SensorRepository;
import ru.yandex.practicum.service.handler.EventHandler;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements EventHandler<HubEventAvro> {
    private final SensorRepository sensorRepository;

    @Override
    public String getPayloadCase() {
        return DeviceRemovedEventAvro.class.getName();
    }

    @Override
    public void handle(HubEventAvro message) {
        DeviceRemovedEventAvro payload = (DeviceRemovedEventAvro) message.getPayload();
        handleDeviceRemoved(message.getHubId(), payload);
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro payload) {
        Optional<Sensor> found = sensorRepository.findByIdAndHubId(payload.getId(), hubId);
        if (found.isPresent()) {
            sensorRepository.deleteById(payload.getId());
        }
    }
}