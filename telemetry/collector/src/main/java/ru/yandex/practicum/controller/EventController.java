package ru.yandex.practicum.controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.handler.HubEventHandler;
import ru.yandex.practicum.handler.SensorEventHandler;

@Slf4j
@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubHandlers;
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorHandlers;

    public EventController(List<HubEventHandler> hubHandlers, List<SensorEventHandler> sensorHandlers) {
        this.hubHandlers = hubHandlers.stream().collect(Collectors.toMap(HubEventHandler::getEventType,
                Function.identity()));
        this.sensorHandlers = sensorHandlers.stream().collect(Collectors.toMap(SensorEventHandler::getEventType,
                Function.identity()));
    }

    @Override
    public void collectSensorEvent(SensorEventProto event, StreamObserver<Empty> responseObserver) {
        try {
            log.info("-> Sensor event: {}", event);
            sensorHandlers.get(event.getPayloadCase()).handle(event);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto event, StreamObserver<Empty> responseObserver) {
        try {
            log.info("-> Hub event: {}", event);
            hubHandlers.get(event.getPayloadCase()).handle(event);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}