package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.handler.EventHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HubServiceImpl implements HubService {
    private final Map<String, EventHandler<HubEventAvro>> eventHandlerMap;

    public HubServiceImpl(List<EventHandler<HubEventAvro>> eventHandlers) {
        eventHandlerMap = eventHandlers.stream().collect(Collectors.toMap(EventHandler<HubEventAvro>::getPayloadCase, Function.identity()));
    }

    @Override
    public void handleMessage(HubEventAvro message) {
        String name = message.getPayload().getClass().getName();
        if (eventHandlerMap.get(name) != null) {
            eventHandlerMap.get(name).handle(message);
        }
    }
}