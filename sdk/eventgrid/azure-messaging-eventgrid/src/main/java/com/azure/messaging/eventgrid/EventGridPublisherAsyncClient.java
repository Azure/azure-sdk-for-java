// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImpl;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImplBuilder;
import com.azure.messaging.eventgrid.models.CloudEvent;
import com.azure.messaging.eventgrid.models.EventGridEvent;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * A service client that publishes events to an EventGrid topic or domain. Use {@link EventGridPublisherClientBuilder}
 * to create an instance of this client. This uses Project Reactor (https://projectreactor.io/) to handle asynchronous
 * programming.
 * @see EventGridEvent
 * @see CloudEvent
 */
@ServiceClient(builder = EventGridPublisherClientBuilder.class, isAsync = true)
public final class EventGridPublisherAsyncClient {

    private final String hostname;

    private final EventGridPublisherClientImpl impl;

    EventGridPublisherAsyncClient(HttpPipeline pipeline, String hostname, SerializerAdapter serializerAdapter) {
        this.impl = new EventGridPublisherClientImplBuilder()
            .pipeline(pipeline)
            .serializerAdapter(serializerAdapter)
            .buildClient();

        this.hostname = hostname;
    }

    /**
     * Get the service version of the Rest API.
     * @return the Service version of the rest API
     */
    public EventGridServiceVersion getServiceVersion() {
        return EventGridServiceVersion.V2018_01_01;
    }

    /**
     * Publishes the given EventGrid events to the set topic or domain.
     * @param events the EventGrid events to publish.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendEvents(Iterable<EventGridEvent> events) {
        return withContext(context -> publishEvents(events, context));
    }

    Mono<Void> publishEvents(Iterable<EventGridEvent> events, Context context) {
        List<com.azure.messaging.eventgrid.implementation.models.EventGridEvent> implList = new ArrayList<>();
        for (EventGridEvent event : events) {
            implList.add(toImpl(event));
        }
        return impl.publishEventsAsync(hostname, implList, context);
    }

    private com.azure.messaging.eventgrid.implementation.models.EventGridEvent toImpl(EventGridEvent event) {
        return new com.azure.messaging.eventgrid.implementation.models.EventGridEvent()
            .setData(event.getData())
            .setEventTime(event.getEventTime())
            .setTopic(event.getTopic())
            .setId(event.getId())
            .setDataVersion(event.getDataVersion())
            .setEventType(event.getEventType())
            .setSubject(event.getSubject());
    }

    /**
     * Publishes the given cloud events to the set topic or domain.
     * @param events the cloud events to publish.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendCloudEvents(Iterable<CloudEvent> events) {
        return withContext(context -> publishCloudEvents(events, context));
    }

    Mono<Void> publishCloudEvents(Iterable<CloudEvent> events, Context context) {
        List<com.azure.messaging.eventgrid.implementation.models.CloudEvent> implList = new ArrayList<>();
        for (CloudEvent event : events) {
            implList.add(toImpl(event));
        }

        return impl.publishCloudEventEventsAsync(hostname, implList, context);
    }

    private com.azure.messaging.eventgrid.implementation.models.CloudEvent toImpl(CloudEvent event) {
        return new com.azure.messaging.eventgrid.implementation.models.CloudEvent()
            .setDatacontenttype(event.getDataContentType())
            .setData(event.getData())
            .setAdditionalProperties(event.getExtensionAttributes())
            .setDataBase64(Base64.getEncoder().encodeToString(event.getBinaryData()))
            .setSubject(event.getSubject())
            .setDataschema(event.getDataSchema())
            .setTime(event.getTime())
            .setType(event.getType())
            .setSpecversion(event.getSpecVersion())
            .setSource(event.getSource())
            .setId(event.getId());
    }

    /**
     * Publishes the given custom events to the set topic or domain.
     * @param events the custom events to publish.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendCustomEvents(Iterable<Object> events) {
        return withContext(context -> publishCustomEvents(events, context));
    }

    Mono<Void> publishCustomEvents(Iterable<Object> events, Context context) {
        List<Object> objList = new ArrayList<>();

        for (Object event : events) {
            objList.add(event);
        }
        return impl.publishCustomEventEventsAsync(hostname, objList, context);
    }

    /**
     * Publishes the given EventGrid events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the EventGrid events to publish.
     *
     * @return the response from the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendEventsWithResponse(Iterable<EventGridEvent> events) {
        return withContext(context -> publishEventsWithResponse(events, context));
    }

    Mono<Response<Void>> publishEventsWithResponse(Iterable<EventGridEvent> events, Context context) {
        List<com.azure.messaging.eventgrid.implementation.models.EventGridEvent> implList = new ArrayList<>();
        for (EventGridEvent event : events) {
            implList.add(toImpl(event));
        }
        return impl.publishEventsWithResponseAsync(hostname, implList, context);
    }

    /**
     * Publishes the given cloud events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the cloud events to publish.
     *
     * @return the response from the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendCloudEventsWithResponse(Iterable<CloudEvent> events) {
        return withContext(context -> publishCloudEventsWithResponse(events, context));
    }

    Mono<Response<Void>> publishCloudEventsWithResponse(Iterable<CloudEvent> events, Context context) {
        List<com.azure.messaging.eventgrid.implementation.models.CloudEvent> implList = new ArrayList<>();
        for (CloudEvent event : events) {
            implList.add(toImpl(event));
        }

        return impl.publishCloudEventEventsWithResponseAsync(hostname, implList, context);
    }

    /**
     * Publishes the given custom events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the custom events to publish.
     *
     * @return the response from the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendCustomEventsWithResponse(Iterable<Object> events) {
        return withContext(context -> publishCustomEventsWithResponse(events, context));
    }

    Mono<Response<Void>> publishCustomEventsWithResponse(Iterable<Object> events, Context context) {
        List<Object> objList = new ArrayList<>();

        for (Object event : events) {
            objList.add(event);
        }
        return impl.publishCustomEventEventsWithResponseAsync(hostname, objList, context);
    }
}
