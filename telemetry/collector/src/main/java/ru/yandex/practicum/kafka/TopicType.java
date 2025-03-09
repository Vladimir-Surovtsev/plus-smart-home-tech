package ru.yandex.practicum.kafka;

public enum TopicType {
    SENSORS_EVENTS,
    HUBS_EVENTS;

    public static TopicType from(String type) {
        switch (type) {
            case "sensor-events" -> {
                return TopicType.SENSORS_EVENTS;
            }
            case "hubs-events" -> {
                return TopicType.HUBS_EVENTS;
            }
            default -> throw new RuntimeException("Topic type not found");

        }
    }
}