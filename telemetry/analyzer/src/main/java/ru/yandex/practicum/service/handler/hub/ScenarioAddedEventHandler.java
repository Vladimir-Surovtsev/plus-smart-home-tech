package ru.yandex.practicum.service.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.ScenarioAction;
import ru.yandex.practicum.model.ScenarioActionId;
import ru.yandex.practicum.model.ScenarioCondition;
import ru.yandex.practicum.model.ScenarioConditionId;
import ru.yandex.practicum.repository.ActionRepository;
import ru.yandex.practicum.repository.ConditionRepository;
import ru.yandex.practicum.repository.ScenarioActionRepository;
import ru.yandex.practicum.repository.ScenarioConditionRepository;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;
import ru.yandex.practicum.service.handler.EventHandler;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements EventHandler<HubEventAvro> {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    @Override
    public String getPayloadCase() {
        return ScenarioAddedEventAvro.class.getName();
    }

    @Override
    public void handle(HubEventAvro message) {
        ScenarioAddedEventAvro payload = (ScenarioAddedEventAvro) message.getPayload();
        handleScenarioAdded(message.getHubId(), payload);
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro payload) {
        try {
            Scenario scenario = Scenario.builder()
                    .hubId(hubId)
                    .name(payload.getName())
                    .build();

            scenarioRepository.save(scenario);

            saveConditions(hubId, scenario, payload.getConditions());

            saveActions(hubId, scenario, payload.getActions());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveConditions(String hubId, Scenario scenario, List<ScenarioConditionAvro> conditionAvroList) {
        List<Condition> conditionList = new ArrayList<>();
        List<ScenarioCondition> scenarioConditionList = new ArrayList<>();

        for (ScenarioConditionAvro condition : conditionAvroList) {
            Condition c = Condition.builder()
                    .type(condition.getType())
                    .operation(condition.getOperation())
                    .value(getIntegerValue(condition.getValue()))
                    .build();
            ScenarioCondition scenarioCondition = ScenarioCondition.builder()
                    .id(new ScenarioConditionId())
                    .scenario(scenario)
                    .condition(c)
                    .sensor(sensorRepository.findByIdAndHubId(condition.getSensorId(), hubId).orElseThrow())
                    .build();
            conditionList.add(c);
            scenarioConditionList.add(scenarioCondition);
        }

        conditionRepository.saveAll(conditionList);
        scenarioConditionRepository.saveAll(scenarioConditionList);
    }

    private void saveActions(String hubId, Scenario scenario, List<DeviceActionAvro> actionAvroList) {
        List<Action> actionList = new ArrayList<>();
        List<ScenarioAction> scenarioActionList = new ArrayList<>();

        for (DeviceActionAvro action : actionAvroList) {
            Action a = Action.builder()
                    .type(action.getType())
                    .value(action.getValue())
                    .build();
            actionList.add(a);
            ScenarioAction scenarioAction = ScenarioAction.builder()
                    .id(new ScenarioActionId())
                    .scenario(scenario)
                    .action(a)
                    .sensor(sensorRepository.findByIdAndHubId(action.getSensorId(), hubId).orElseThrow())
                    .build();
            scenarioActionList.add(scenarioAction);
        }

        actionRepository.saveAll(actionList);
        scenarioActionRepository.saveAll(scenarioActionList);
    }


    private Integer getIntegerValue(Object payload) {
        if (payload instanceof Integer) {
            return (int) payload;
        }
        if (payload instanceof Boolean) {
            return (boolean) payload ? 1 : 0;
        }
        return null;
    }
}