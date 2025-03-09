package ru.yandex.practicum.analyzer.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.repository.entity.Action;
import ru.yandex.practicum.analyzer.repository.entity.ActionType;
import ru.yandex.practicum.analyzer.repository.entity.Condition;
import ru.yandex.practicum.analyzer.repository.entity.ConditionOperation;
import ru.yandex.practicum.analyzer.repository.entity.ConditionType;
import ru.yandex.practicum.analyzer.repository.entity.Scenario;

@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;

    public List<Scenario> getScenariosByHubId(String hubId) {
        return scenarioRepository.findByHubId(hubId);
    }

    public void addScenario(ScenarioAddedEventAvro event, String hubId) {
        Scenario scenario = new Scenario();
        scenario.setName(event.getName());
        scenario.setHubId(hubId);

        List<Condition> conditions = event.getConditions().stream()
                .map(conditionEvent -> Condition.builder()
                        .type(ConditionType.valueOf(conditionEvent.getType().name()))
                        .operation(ConditionOperation.valueOf(conditionEvent.getOperation().name()))
                        .value(convertToInteger(conditionEvent.getValue())) // Преобразование value
                        .scenario(scenario)
                        .sensorId(conditionEvent.getSensorId())
                        .build())
                .toList();

        List<Action> actions = event.getActions().stream()
                .map(actionEvent -> Action.builder()
                        .type(ActionType.valueOf(actionEvent.getType().name()))
                        .value(Optional.ofNullable(actionEvent.getValue()).orElse(0))
                        .scenario(scenario)
                        .sensorId(actionEvent.getSensorId())
                        .build())
                .toList();

        scenario.setConditions(conditions);
        scenario.setActions(actions);

        scenarioRepository.save(scenario);
    }

    public void deleteScenario(String name) {
        scenarioRepository.deleteByName(name);
    }

    private Integer convertToInteger(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else {
            return null;
        }
    }
}