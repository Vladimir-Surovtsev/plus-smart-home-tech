package ru.yandex.practicum.service.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.service.handler.EventHandler;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements EventHandler<HubEventAvro> {
    private final ScenarioRepository scenarioRepository;

    @Override
    public String getPayloadCase() {
        return ScenarioRemovedEventAvro.class.getName();
    }

    @Override
    public void handle(HubEventAvro message) {
        ScenarioRemovedEventAvro payload = (ScenarioRemovedEventAvro) message.getPayload();
        handleScenarioRemoved(message.getHubId(), payload);
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro payload) {
        Optional<Scenario> scenario = scenarioRepository.findByIdAndHubId((long) payload.getId(), hubId);
        scenario.ifPresent(value -> scenarioRepository.deleteById(value.getId()));
    }
}