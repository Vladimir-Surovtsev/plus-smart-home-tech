package ru.yandex.kafka;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@SuppressWarnings("ALL")
public class HubEventDeserializer extends BaseAvroDeserializer<HubEventAvro> {
    public HubEventDeserializer() {
        super(HubEventAvro.getClassSchema());
    }
}