package ru.yandex.practicum.event.model.hub;

import lombok.Data;

@Data
public class ScenarioRemovedEvent extends HubEvent {
    private Integer id;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}