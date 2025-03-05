package ru.yandex.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.KafkaTopics;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.HubService;
import ru.yandex.practicum.service.SnapshotService;

@Component
@RequiredArgsConstructor
public class KafkaListenerHandler {
    private final HubService hubService;
    private final SnapshotService snapshotService;

    @KafkaListener(topics = KafkaTopics.SNAPSHOTS)
    public void listener(SensorsSnapshotAvro msg) {
        snapshotService.handleMessage(msg);
    }

    @KafkaListener(topics = KafkaTopics.HUBS)
    public void listener(HubEventAvro msg) {
        hubService.handleMessage(msg);
    }
}