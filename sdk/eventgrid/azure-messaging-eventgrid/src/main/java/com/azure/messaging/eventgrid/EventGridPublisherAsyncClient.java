package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.Response;
import com.azure.messaging.eventgrid.models.CloudEvent;
import com.azure.messaging.eventgrid.models.EventGridEvent;
import reactor.core.publisher.Mono;

import java.util.List;

@ServiceClient(builder = EventGridPublisherClientBuilder.class, isAsync = true)
public class EventGridPublisherAsyncClient {

    public Mono<Void> publishEvents(List<EventGridEvent> events) {
        // TODO: implement method
        return null;
    }

    public Mono<Void> publishCloudEvents(List<CloudEvent> events) {
        // TODO: implement method
        return null;
    }

    public Mono<Void> publishCustomEvents(List<EventSchema> events) {
        // TODO: implement method
        return null;
    }

    public Mono<Response<Void>> publishEventsWithResponse(List<EventGridEvent> events) {
        // TODO: implement method
        return null;
    }

    public Mono<Response<Void>> publishCloudEventsWithResponse(List<CloudEvent> events) {
        // TODO: implement method
        return null;
    }

    public Mono<Response<Void>> publishCustomEventsWithResponse(List<EventSchema> events) {
        // TODO: implement method
        return null;
    }
}
