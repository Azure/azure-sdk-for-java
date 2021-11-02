// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.cloudnative.cloudevents;

import com.azure.core.http.rest.Response;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.List;

/**
 * EventGrid Cloud Native Event Publisher
 */
public final class EventGridCloudNativeEventPublisher {
    /**
     *
     *
     * @param syncClient
     * @param event
     */
    public static void sendEvent(EventGridPublisherClient<com.azure.core.models.CloudEvent> syncClient,
        CloudEvent event) {
        syncClient.sendEvent(toEventGridCloudEvent(event));
    }

    /**
     *
     *
     * @param asyncClient
     * @param event
     * @return
     */
    public static Mono<Void> sendEventAsync(EventGridPublisherAsyncClient<com.azure.core.models.CloudEvent> asyncClient,
        CloudEvent event) {
        return asyncClient.sendEvent(toEventGridCloudEvent(event));
    }

    /**
     *
     *
     * @param syncClient
     * @param events
     */
    public static void sendEvents(EventGridPublisherClient<com.azure.core.models.CloudEvent> syncClient,
        Iterable<CloudEvent> events) {
        syncClient.sendEvents(toEventGridCloudEvents(events));
    }

    /**
     *
     * @param asyncClient
     * @param events
     * @return
     */
    public static Mono<Void> sendEventsAsync(
        EventGridPublisherAsyncClient<com.azure.core.models.CloudEvent> asyncClient, Iterable<CloudEvent> events) {
        return asyncClient.sendEvents(toEventGridCloudEvents(events));
    }

    /**
     *
     * @param syncClient
     * @param events
     * @param context
     */
    public static void sendEventsWithResponse(EventGridPublisherClient<com.azure.core.models.CloudEvent> syncClient,
        Iterable<CloudEvent> events, Context context) {
        syncClient.sendEventsWithResponse(toEventGridCloudEvents(events), context);
    }

    /**
     *
     * @param asyncClient
     * @param events
     * @return
     */
    public static Mono<Response<Void>> sendEventsWithResponseAsync(
        EventGridPublisherAsyncClient<com.azure.core.models.CloudEvent> asyncClient, Iterable<CloudEvent> events) {
        return asyncClient.sendEventsWithResponse(toEventGridCloudEvents(events));
    }

    private static Iterable<com.azure.core.models.CloudEvent> toEventGridCloudEvents(Iterable<CloudEvent> events) {
        List<com.azure.core.models.CloudEvent> cloudEvents = new ArrayList<>();
        for (CloudEvent event : events) {
            cloudEvents.add(toEventGridCloudEvent(event));
        }
        return cloudEvents;
    }

    private static com.azure.core.models.CloudEvent toEventGridCloudEvent(CloudEvent event) {
        if (event == null) {
            return null;
        }
        // io.cloudevents.CloudEvent's id, source, type, and specversion are required.
        // azure CloudEvent's  source, type, and format(if data exist) are required.
        final com.azure.core.models.CloudEvent cloudEvent = new com.azure.core.models.CloudEvent(
            event.getSource().toString(), // required
            event.getType(), // required
            event.getData() == null ? null : BinaryData.fromObject(event.getData()),
            CloudEventDataFormat.JSON, // TODO: what about BYTE
            event.getDataContentType() == null ? null : event.getDataContentType()
        );

        CloudEventData cloudEventData = event.getData();
//        EventFormat format = EventFormatProvider
//                                 .getInstance()
//                                 .resolveFormat(JsonFormat.CONTENT_TYPE);

// Serialize event
//        byte[] serialized = format.serialize(event);

        if (event.getSubject() != null) {
            cloudEvent.setSubject(event.getSubject());
        }
        if (event.getDataSchema() != null) {
            cloudEvent.setDataSchema(event.getDataSchema().toString());
        }
        if (event.getTime() != null) {
            cloudEvent.setTime(event.getTime());
        }

        cloudEvent.setId(event.getId()); // required
        // SpecVersion is internally set by Azure CloudEvent to 1.0
        return cloudEvent;
    }
}
