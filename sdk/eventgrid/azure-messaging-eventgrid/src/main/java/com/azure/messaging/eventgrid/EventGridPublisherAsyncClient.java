// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    private final EventGridServiceVersion serviceVersion;

    EventGridPublisherAsyncClient(HttpPipeline pipeline, String hostname, SerializerAdapter serializerAdapter,
                                  EventGridServiceVersion serviceVersion) {
        this.impl = new EventGridPublisherClientImplBuilder()
            .pipeline(pipeline)
            .serializerAdapter(serializerAdapter)
            .buildClient();

        // currently the service version is hardcoded into the Impl client, but once another service version gets
        // released we should add this to the impl builder options
        this.serviceVersion = serviceVersion;

        this.hostname = hostname;
    }

    /**
     * Get the service version of the Rest API.
     * @return the Service version of the rest API
     */
    public EventGridServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }

    /**
     * Publishes the given EventGrid events to the set topic or domain.
     * @param events the EventGrid events to publish.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendEvents(Iterable<EventGridEvent> events) {
        return withContext(context -> sendEvents(events, context));
    }

    Mono<Void> sendEvents(Iterable<EventGridEvent> events, Context context) {
        return Flux.fromIterable(events)
            .map(EventGridEvent::toImpl)
            .collectList()
            .flatMap(list -> this.impl.publishEventsAsync(this.hostname, list, context));
    }

    /**
     * Publishes the given cloud events to the set topic or domain.
     * @param events the cloud events to publish.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendCloudEvents(Iterable<CloudEvent> events) {
        return withContext(context -> sendCloudEvents(events, context));
    }

    Mono<Void> sendCloudEvents(Iterable<CloudEvent> events, Context context) {
        return Flux.fromIterable(events)
            .map(CloudEvent::toImpl)
            .collectList()
            .flatMap(list -> this.impl.publishCloudEventEventsAsync(this.hostname, list, context));
    }

    /**
     * Publishes the given custom events to the set topic or domain.
     * @param events the custom events to publish.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendCustomEvents(Iterable<Object> events) {
        return withContext(context -> sendCustomEvents(events, context));
    }

    Mono<Void> sendCustomEvents(Iterable<Object> events, Context context) {
        return Flux.fromIterable(events)
            .collectList()
            .flatMap(list -> this.impl.publishCustomEventEventsAsync(this.hostname, list, context));
    }

    /**
     * Publishes the given EventGrid events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the EventGrid events to publish.
     *
     * @return the response from the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendEventsWithResponse(Iterable<EventGridEvent> events) {
        return withContext(context -> sendEventsWithResponse(events, context));
    }

    Mono<Response<Void>> sendEventsWithResponse(Iterable<EventGridEvent> events, Context context) {
        return Flux.fromIterable(events)
            .map(EventGridEvent::toImpl)
            .collectList()
            .flatMap(list -> this.impl.publishEventsWithResponseAsync(this.hostname, list, context));
    }

    /**
     * Publishes the given cloud events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the cloud events to publish.
     *
     * @return the response from the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendCloudEventsWithResponse(Iterable<CloudEvent> events) {
        return withContext(context -> sendCloudEventsWithResponse(events, context));
    }

    Mono<Response<Void>> sendCloudEventsWithResponse(Iterable<CloudEvent> events, Context context) {
        return Flux.fromIterable(events)
            .map(CloudEvent::toImpl)
            .collectList()
            .flatMap(list -> this.impl.publishCloudEventEventsWithResponseAsync(this.hostname, list, context));
    }

    /**
     * Publishes the given custom events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the custom events to publish.
     *
     * @return the response from the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendCustomEventsWithResponse(Iterable<Object> events) {
        return withContext(context -> sendCustomEventsWithResponse(events, context));
    }

    Mono<Response<Void>> sendCustomEventsWithResponse(Iterable<Object> events, Context context) {
        return Flux.fromIterable(events)
            .collectList()
            .flatMap(list -> this.impl.publishCustomEventEventsWithResponseAsync(this.hostname, list, context));
    }
}
