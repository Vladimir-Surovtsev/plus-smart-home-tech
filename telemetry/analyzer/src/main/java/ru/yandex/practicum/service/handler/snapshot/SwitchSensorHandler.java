package ru.yandex.practicum.service.handler.snapshot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.model.ScenarioAction;
import ru.yandex.practicum.model.ScenarioCondition;
import ru.yandex.practicum.repository.ScenarioActionRepository;
import ru.yandex.practicum.repository.ScenarioConditionRepository;
import ru.yandex.practicum.service.AnalyzerGrpcService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SwitchSensorHandler extends BaseSensorHandler implements SnapshotStateHandler<SwitchSensorAvro> {
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;
    private final AnalyzerGrpcService analyzerGrpcService;

    @Override
    public String getType() {
        return SwitchSensorAvro.class.getSimpleName();
    }

    @Override
    public void handle(String hubId, String sensorId, SwitchSensorAvro payload) {
        List<ScenarioCondition> scenarioConditions = scenarioConditionRepository.findAllBySensorIdAndSensorHubId(sensorId, hubId);

        if (scenarioConditions.isEmpty()) {
            return;
        }

        for (ScenarioCondition scenarioCondition : scenarioConditions) {
            ConditionOperationAvro operation = scenarioCondition.getCondition().getOperation();
            String scenarioName = scenarioCondition.getScenario().getName();
            List<ScenarioAction> scenarioActions = scenarioActionRepository.findAllByScenarioId(scenarioCondition.getScenario().getId());

            if (scenarioActions.isEmpty()) {
                return;
            }

            if (operation != ConditionOperationAvro.EQUALS) {
                throw new IllegalStateException("Unexpected value: " + operation);
            }

            if (scenarioCondition.getCondition().getValue() > 0 == payload.getState()) {
                executeScenarioActions(hubId, scenarioActions, scenarioName, analyzerGrpcService);
            }
        }
    }
}