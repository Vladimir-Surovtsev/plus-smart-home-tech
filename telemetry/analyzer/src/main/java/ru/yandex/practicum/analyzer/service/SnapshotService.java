package ru.yandex.practicum.analyzer.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.analyzer.client.HubRouterClient;
import ru.yandex.practicum.analyzer.repository.entity.Action;
import ru.yandex.practicum.analyzer.repository.entity.Condition;
import ru.yandex.practicum.analyzer.repository.entity.ConditionOperation;
import ru.yandex.practicum.analyzer.repository.entity.Scenario;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotService {

    private ScenarioService scenarioService;
    private HubRouterClient hubRouterClient;

    public void analyze(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        List<Scenario> scenarios = scenarioService.getScenariosByHubId(hubId);

        scenarios.stream()
                .filter(scenario -> isScenarioTriggered(scenario, snapshot))
                .forEach(scenario -> executeActions(scenario.getActions(), hubId));
    }

    private boolean isScenarioTriggered(Scenario scenario, SensorsSnapshotAvro snapshot) {
        for (Condition condition : scenario.getConditions()) {
            if (!checkCondition(condition, snapshot)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkCondition(Condition condition, SensorsSnapshotAvro snapshot) {
        SensorStateAvro record = snapshot.getSensorsState().get(condition.getSensorId());

        if (record == null) {
            log.warn("Sensor data for sensorId {} is missing in the snapshot", condition.getSensorId());
            return false;
        }

        try {
            return switch (condition.getType()) {
                case TEMPERATURE -> {
                    TemperatureSensorAvro tempSensor = (TemperatureSensorAvro) record.getData();
                    yield evaluateCondition(tempSensor.getTemperatureC(), condition.getOperation(),
                            condition.getValue());
                }
                case HUMIDITY -> {
                    ClimateSensorAvro climateSensor = (ClimateSensorAvro) record.getData();
                    yield evaluateCondition(climateSensor.getHumidity(), condition.getOperation(),
                            condition.getValue());
                }
                case CO2LEVEL -> {
                    ClimateSensorAvro climateSensor = (ClimateSensorAvro) record.getData();
                    yield evaluateCondition(climateSensor.getCo2Level(), condition.getOperation(),
                            condition.getValue());
                }
                case LUMINOSITY -> {
                    LightSensorAvro lightSensor = (LightSensorAvro) record.getData();
                    yield evaluateCondition(lightSensor.getLuminosity(), condition.getOperation(),
                            condition.getValue());
                }
                case MOTION -> {
                    MotionSensorAvro motionSensor = (MotionSensorAvro) record.getData();
                    int motionValue = motionSensor.getMotion() ? 1 : 0;
                    yield evaluateCondition(motionValue, condition.getOperation(), condition.getValue());
                }
                case SWITCH -> {
                    SwitchSensorAvro switchSensor = (SwitchSensorAvro) record.getData();
                    int switchState = switchSensor.getState() ? 1 : 0;
                    yield evaluateCondition(switchState, condition.getOperation(), condition.getValue());
                }
            };
        } catch (Exception e) {
            log.error("Error checking condition {}: {}", condition, e.getMessage());
        }
        return false;
    }

    private boolean evaluateCondition(int sensorValue, ConditionOperation operation, int targetValue) {
        return switch (operation) {
            case EQUALS -> sensorValue == targetValue;
            case GREATER_THAN -> sensorValue > targetValue;
            case LOWER_THAN -> sensorValue < targetValue;
        };
    }

    private void executeActions(List<Action> actions, String hubId) {
        for (Action action : actions) {
            hubRouterClient.executeAction(action, hubId);
            log.info("Executing action: {} for hubId: {}", action, hubId);
        }
    }
}