package ru.yandex.practicum.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.repository.SensorRepository;
import ru.yandex.practicum.repository.entity.Sensor;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorRepository sensorRepository;

    public Optional<Sensor> getSensorByIdAndHubId(String sensorId, String hubId) {
        return sensorRepository.findByIdAndHubId(sensorId, hubId);
    }

    public boolean existsBySensorIdsAndHubId(String hubId, String sensorId) {
        return sensorRepository.existsByIdInAndHubId(List.of(sensorId), hubId);
    }

    public void addSensor(String sensorId, String hubId) {
        if (existsBySensorIdsAndHubId(hubId, sensorId)) {
            return;
        }
        Sensor sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setHubId(hubId);
        sensorRepository.save(sensor);
    }

    public void removeSensor(String sensorId, String hubId) {
        getSensorByIdAndHubId(sensorId, hubId).ifPresent(sensorRepository::delete);
    }
}