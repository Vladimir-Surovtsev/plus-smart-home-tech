package ru.yandex.practicum.service.handler.snapshot;

import com.google.protobuf.Timestamp;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.model.ScenarioAction;
import ru.yandex.practicum.service.AnalyzerGrpcService;

import java.time.Instant;
import java.util.List;

public class BaseSensorHandler {
    void executeScenarioActions(String hubId, List<ScenarioAction> scenarioActions, String scenarioName, AnalyzerGrpcService analyzerGrpcService) {
        scenarioActions.forEach(sa -> {
            DeviceActionProto deviceActionProto = DeviceActionProto.newBuilder()
                    .setSensorId(sa.getSensor().getId())
                    .setType(ActionTypeProto.valueOf(sa.getAction().getType().name()))
                    .setValue(sa.getAction().getValue())
                    .build();
            DeviceActionRequest deviceActionRequest = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenarioName)
                    .setAction(deviceActionProto)
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())).build();

            analyzerGrpcService.handleDeviceRequest(deviceActionRequest);
        });
    }
}