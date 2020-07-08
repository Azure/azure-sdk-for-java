package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.Response;
import com.azure.messaging.eventgrid.models.CloudEvent;
import com.azure.messaging.eventgrid.models.EventGridEvent;

import java.util.List;

@ServiceClient(builder = EventGridPublisherClientBuilder.class)
public class EventGridPublisherClient {

    public void publishEvents(List<EventGridEvent> events) {
        // TODO: implement method
    }

    public void publishCloudEvents(List<CloudEvent> events) {
        // TODO: implement method
    }

    public void publishCustomEvents(List<EventSchema> events) {
        // TODO: implement method
    }

    public Response<Void> publishEventsWithResponse(List<EventGridEvent> events) {
        // TODO: implement method
        return null;
    }

    public Response<Void> publishCloudEventsWithResponse(List<CloudEvent> events) {
        // TODO: implement method
        return null;
    }

    public Response<Void> publishCustomEventsWithResponse(List<EventSchema> events) {
        // TODO: implement method
        return null;
    }
}
