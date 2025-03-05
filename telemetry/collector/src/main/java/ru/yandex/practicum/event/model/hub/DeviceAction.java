package ru.yandex.practicum.event.model.hub;

import lombok.Data;

@Data
public class DeviceAction {
    private String sensorId;
    private ActionType type;
    private int value;
}