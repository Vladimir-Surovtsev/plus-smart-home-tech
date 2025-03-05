package ru.yandex.practicum.event.model.hub.handler;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

public interface HubEventHandler {
    HubEventProto.PayloadCase getPayloadCase();

    void handle(HubEventProto event);
}