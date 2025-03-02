package event.model.hub;

import lombok.Data;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

@Data
public class DeviceAddedEvent extends HubEvent {
    private String id;
    private DeviceTypeAvro deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }
}