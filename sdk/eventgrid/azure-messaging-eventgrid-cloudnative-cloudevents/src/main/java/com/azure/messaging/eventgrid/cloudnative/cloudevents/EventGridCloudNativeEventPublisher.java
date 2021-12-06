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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EventGrid Cloud Native Event Publisher
 */
public final class EventGridCloudNativeEventPublisher {
    private static final Pattern PATTERN;
    static {
        PATTERN = Pattern.compile("[*/|*/*+]json", Pattern.MULTILINE);
    }

    /**
     * Publishes the given native cloud event to the set topic or domain.
     *
     * @param syncClient a service client that publishes events to an EventGrid topic or domain.
     * @param event the native cloud event to publish.
     */
    public static void sendEvent(EventGridPublisherClient<com.azure.core.models.CloudEvent> syncClient,
        CloudEvent event) {
        syncClient.sendEvent(toEventGridCloudEvent(event));
    }

    /**
     * Publishes the given native cloud event to the set topic or domain.
     *
     * @param asyncClient a service asynchronous client that publishes events to an EventGrid topic or domain.
     * @param event the native cloud event to publish.
     * @return a Mono that completes when the events are sent to the service.
     */
    public static Mono<Void> sendEventAsync(EventGridPublisherAsyncClient<com.azure.core.models.CloudEvent> asyncClient,
        CloudEvent event) {
        return asyncClient.sendEvent(toEventGridCloudEvent(event));
    }

    /**
     * Publishes the given native cloud events to the set topic or domain.
     *
     * @param syncClient a service client that publishes events to an EventGrid topic or domain.
     * @param events the native cloud events to publish.
     */
    public static void sendEvents(EventGridPublisherClient<com.azure.core.models.CloudEvent> syncClient,
        Iterable<CloudEvent> events) {
        syncClient.sendEvents(toEventGridCloudEvents(events));
    }

    /**
     * Publishes the given native cloud events to the set topic or domain.
     *
     * @param asyncClient a service asynchronous client that publishes events to an EventGrid topic or domain.
     * @param events the native cloud events to publish.
     * @return a Mono that completes when the events are sent to the service.
     */
    public static Mono<Void> sendEventsAsync(
        EventGridPublisherAsyncClient<com.azure.core.models.CloudEvent> asyncClient, Iterable<CloudEvent> events) {
        return asyncClient.sendEvents(toEventGridCloudEvents(events));
    }

    /**
     * Publishes the given native cloud events to the set topic or domain and gives the response issued by EventGrid.
     *
     * @param syncClient a service client that publishes events to an EventGrid topic or domain.
     * @param events the native cloud events to publish.
     * @param context the context to use along the pipeline.
     */
    public static void sendEventsWithResponse(EventGridPublisherClient<com.azure.core.models.CloudEvent> syncClient,
        Iterable<CloudEvent> events, Context context) {
        syncClient.sendEventsWithResponse(toEventGridCloudEvents(events), context);
    }

    /**
     * Publishes the given native cloud events to the set topic or domain and gives the response issued by EventGrid.
     *
     * @param asyncClient a service asynchronous client that publishes events to an EventGrid topic or domain.
     * @param events the native cloud events to publish.
     * @return the response from the EventGrid service.
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

        // Identify data format by data content type
        CloudEventDataFormat dataFormat;
        final String dataContentType = event.getDataContentType();
        if (dataContentType == null) {
            dataFormat = CloudEventDataFormat.JSON;
        } else {
            // Future version:
            // https://github.com/cloudevents/spec/blob/main/cloudevents/formats/json-format.md#311-payload-serialization
            // That is, a datacontenttype declares JSON-formatted content if its media type, when stripped of parameters,
            // has the form */json or */*+json. If the datacontenttype is unspecified, processing SHOULD proceed as if
            // the datacontenttype had been specified explicitly as application/json.

            // Current version: https://github.com/cloudevents/sdk-java/blob/ff07dd83150deeb5eca5405cb80760b71e7b470c/
            // formats/json-jackson/src/main/java/io/cloudevents/jackson/JsonFormat.java#L146-L149
            final Matcher matcher = PATTERN.matcher(dataContentType);
            if (matcher.find()) {
                dataFormat = CloudEventDataFormat.JSON;
            } else {
                dataFormat = CloudEventDataFormat.BYTES;
            }
        }
        final CloudEventData data = event.getData();
        final BinaryData binaryData; // Create this variable to avoid NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE
        if (data != null) {
            binaryData = BinaryData.fromBytes(data.toBytes());
        } else {
            binaryData = null;
        }

        // io.cloudevents.CloudEvent's id, source, type, and specversion are required.
        // azure CloudEvent's source, type, and format(if data exist) are required.
        final com.azure.core.models.CloudEvent cloudEvent = new com.azure.core.models.CloudEvent(
            event.getSource().toString(), // required
            event.getType(), // required
            binaryData,
            dataFormat,
            event.getDataContentType() == null ? null : event.getDataContentType()
        );

        // optional: subject
        if (event.getSubject() != null) {
            cloudEvent.setSubject(event.getSubject());
        }
        // optional: data schema
        final URI dataSchema = event.getDataSchema();
        if (dataSchema != null) {
            cloudEvent.setDataSchema(dataSchema.toString());
        }
        // optional: time
        if (event.getTime() != null) {
            cloudEvent.setTime(event.getTime());
        }

        cloudEvent.setId(event.getId()); // required

        // optional: extensionNames
        event.getExtensionNames()
            .stream()
            .forEach(name -> cloudEvent.addExtensionAttribute(name, event.getAttribute(name)));
        return cloudEvent;
    }
}
