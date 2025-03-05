package ru.yandex.practicum.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.handler.snapshot.SnapshotStateHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SnapshotServiceImpl implements SnapshotService {
    private final Map<String, SnapshotStateHandler> snapshotStateHandlers;

    public SnapshotServiceImpl(List<SnapshotStateHandler<? extends SpecificRecordBase>> snapshotStateHandlers) {
        this.snapshotStateHandlers = snapshotStateHandlers.stream()
                .collect(Collectors.toMap(SnapshotStateHandler::getType, Function.identity()));
    }

    @Override
    public void handleMessage(SensorsSnapshotAvro message) {
        String hubId = message.getHubId();
        Map<String, SensorStateAvro> sensorIdToState = message.getSensorsState();
        if (sensorIdToState.isEmpty()) {
            return;
        }
        sensorIdToState.forEach((sensorId, state) -> {
            String className = state.getData().getClass().getSimpleName();
            if (snapshotStateHandlers.containsKey(className)) {
                snapshotStateHandlers.get(className).handle(hubId, sensorId, (SpecificRecordBase) state.getData());
            }
        });
    }
}