package event.service.hub;

import event.model.hub.HubEvent;

public interface HubService {
    void sendEventToKafka(HubEvent hubEvent);
}