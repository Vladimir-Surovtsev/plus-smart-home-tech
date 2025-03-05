package ru.yandex.practicum.service.handler.snapshot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.model.ScenarioAction;
import ru.yandex.practicum.model.ScenarioCondition;
import ru.yandex.practicum.repository.ScenarioActionRepository;
import ru.yandex.practicum.repository.ScenarioConditionRepository;
import ru.yandex.practicum.service.AnalyzerGrpcService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClimateSensorHandler extends BaseSensorHandler implements SnapshotStateHandler<ClimateSensorAvro> {
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final AnalyzerGrpcService analyzerGrpcService;
    private final ScenarioActionRepository scenarioActionRepository;

    @Override
    public String getType() {
        return ClimateSensorAvro.class.getSimpleName();
    }

    private boolean checkExpressionByOperation(ConditionOperationAvro operation, int a, int b) {
        switch (operation) {
            case EQUALS -> {
                return a == b;
            }
            case GREATER_THAN -> {
                return a > b;
            }
            case LOWER_THAN -> {
                return a < b;
            }
            case null, default -> throw new IllegalStateException("Unexpected operation: " + operation);
        }
    }

    @Override
    public void handle(String hubId, String sensorId, ClimateSensorAvro payload) {
        List<ScenarioCondition> scenarioConditions = scenarioConditionRepository.findAllBySensorIdAndSensorHubId(sensorId, hubId);

        if (scenarioConditions.isEmpty()) {
            return;
        }

        Map<ConditionOperationAvro, Map<ConditionTypeAvro, Runnable>> conditionOperationToConditionType = scenarioConditions.stream()
                .collect(Collectors.toMap(
                        scenarioCondition -> scenarioCondition.getCondition().getOperation(),
                        scenarioCondition -> Map.of(scenarioCondition.getCondition().getType(), () -> {
                            List<ScenarioAction> scenarioActions = scenarioActionRepository.findAllByScenarioId(scenarioCondition.getScenario().getId());
                            String scenarioName = scenarioCondition.getScenario().getName();

                            ConditionTypeAvro type = scenarioCondition.getCondition().getType();

                            switch (type) {
                                case TEMPERATURE: {
                                    if (checkExpressionByOperation(scenarioCondition.getCondition().getOperation(), payload.getTemperatureC(), scenarioCondition.getCondition().getValue())) {
                                        executeScenarioActions(hubId, scenarioActions, scenarioName, analyzerGrpcService);
                                    }
                                    break;
                                }
                                case HUMIDITY: {
                                    if (checkExpressionByOperation(scenarioCondition.getCondition().getOperation(), payload.getHumidity(), scenarioCondition.getCondition().getValue())) {
                                        executeScenarioActions(hubId, scenarioActions, scenarioName, analyzerGrpcService);
                                    }
                                    break;
                                }
                                case CO2LEVEL: {
                                    if (checkExpressionByOperation(scenarioCondition.getCondition().getOperation(), payload.getCo2Level(), scenarioCondition.getCondition().getValue())) {
                                        executeScenarioActions(hubId, scenarioActions, scenarioName, analyzerGrpcService);
                                    }
                                    break;
                                }
                                case null, default:
                                    throw new IllegalStateException("Unexpected value: " + type);
                            }
                        })
                ));

        conditionOperationToConditionType.values().forEach(i -> i.values().forEach(Runnable::run));
    }

}