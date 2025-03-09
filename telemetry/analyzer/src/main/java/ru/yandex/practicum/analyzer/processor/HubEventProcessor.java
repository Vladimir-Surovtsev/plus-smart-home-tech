package ru.yandex.practicum.analyzer.processor;

import java.time.Duration;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.analyzer.kafka.AnalyzerKafkaConfig;
import ru.yandex.practicum.analyzer.service.ScenarioService;
import ru.yandex.practicum.analyzer.service.SensorService;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, HubEventAvro> hubConsumer;

    private final SensorService sensorService;
    private final ScenarioService scenarioService;

    public HubEventProcessor(AnalyzerKafkaConfig kafkaConfig,
                             SensorService sensorService,
                             ScenarioService scenarioService) {
        hubConsumer = new KafkaConsumer<>(kafkaConfig.getHubConsumerProperties());
        this.sensorService = sensorService;
        this.scenarioService = scenarioService;
    }

    @Override
    public void run() {
        try (hubConsumer) {
            Runtime.getRuntime().addShutdownHook(new Thread(hubConsumer::wakeup));
            hubConsumer.subscribe(Collections.singletonList("telemetry.hubs.v1"));

            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, HubEventAvro> records = hubConsumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    try {
                        onMessage(record);
                    } catch (Exception e) {
                        log.error("Error processing event: {}", record, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in Kafka consumer", e);
        }
    }

    @KafkaListener(topics = "telemetry.hubs.v1", groupId = "analyzer-hub-group")
    public void onMessage(ConsumerRecord<String, HubEventAvro> record) {
        HubEventAvro event = record.value();

        var eventPayload = event.getPayload();

        switch (eventPayload) {
            case DeviceAddedEventAvro deviceAddedEvent ->
                    sensorService.addSensor(deviceAddedEvent.getId(), event.getHubId());
            case DeviceRemovedEventAvro deviceRemovedEvent ->
                    sensorService.removeSensor(deviceRemovedEvent.getId(), event.getHubId());
            case ScenarioAddedEventAvro scenarioAddedEvent ->
                    scenarioService.addScenario(scenarioAddedEvent, event.getHubId());
            case ScenarioRemovedEventAvro scenarioRemovedEvent ->
                    scenarioService.deleteScenario(scenarioRemovedEvent.getName());
            case null, default -> log.warn("Unknown event type: {}", event.getPayload().getClass().getName());
        }
    }
}