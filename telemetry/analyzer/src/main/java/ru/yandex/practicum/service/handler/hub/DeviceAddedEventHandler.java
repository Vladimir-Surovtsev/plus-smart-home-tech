package ru.yandex.practicum.service.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.repository.SensorRepository;
import ru.yandex.practicum.service.handler.EventHandler;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements EventHandler<HubEventAvro> {
    private final SensorRepository sensorRepository;

    @Override
    public String getPayloadCase() {
        return DeviceAddedEventAvro.class.getName();
    }

    @Override
    public void handle(HubEventAvro message) {
        DeviceAddedEventAvro payload = (DeviceAddedEventAvro) message.getPayload();
        handleDeviceAdded(message.getHubId(), payload);
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro payload) {
        Optional<Sensor> found = sensorRepository.findByIdAndHubId(payload.getId(), hubId);
        if (found.isPresent()) {
            return;
        }
        sensorRepository.save(new Sensor(payload.getId(), hubId));
    }
}