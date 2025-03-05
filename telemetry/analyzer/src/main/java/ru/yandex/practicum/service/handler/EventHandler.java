package ru.yandex.practicum.service.handler;

public interface EventHandler<T> {
    String getPayloadCase();

    void handle(T event);
}