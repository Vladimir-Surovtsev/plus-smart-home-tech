package ru.yandex.practicum.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Service
public class AnalyzerGrpcService {
    private final HubRouterControllerGrpc.HubRouterControllerFutureStub hubRouterClient;

    public AnalyzerGrpcService(@GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerFutureStub hubRouterClient) {
        this.hubRouterClient = hubRouterClient;
    }

    public void handleDeviceRequest(DeviceActionRequest request) {
        hubRouterClient.handleDeviceAction(request);
    }
}