package ru.yandex.practicum.event.service.hub;

import ru.yandex.practicum.event.model.hub.HubEvent;

public interface HubService {
    void sendEventToKafka(HubEvent hubEvent);
}