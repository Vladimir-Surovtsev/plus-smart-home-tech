package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.service.SnapshotService;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor implements Runnable {
    private final SnapshotService snapshotService;

    @Override
    public void run() {
        log.info("Snapshot processor started");
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("SnapshotProcessor interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @KafkaListener(topics = "telemetry.snapshots.v1", groupId = "telemetry-snapshots")
    public void onMessage(ConsumerRecord<String, SensorsSnapshotAvro> record) {
        SensorsSnapshotAvro snapshot = record.value();
        log.info("Received snapshot: {}", snapshot);

        snapshotService.analyze(snapshot);
    }
}