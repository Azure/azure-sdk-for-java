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
import com.azure.messaging.eventgrid.events.CloudEvent;
import com.azure.messaging.eventgrid.events.EventGridEvent;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImpl;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImplBuilder;
import reactor.core.publisher.Mono;

/**
 * A service client that publishes events to an EventGrid topic or domain. Use {@link EventGridPublisherClientBuilder}
 * to create an instance of this client. This uses Project Reactor (https://projectreactor.io/) to handle asynchronous
 * programming.
 * @see EventGridEvent
 * @see CloudEvent
 */
@ServiceClient(builder = EventGridPublisherClientBuilder.class, isAsync = true)
public class EventGridPublisherAsyncClient {

    private String endpoint;

    private EventGridPublisherClientImpl impl;

    EventGridPublisherAsyncClient(HttpPipeline pipeline, String endpoint) {
        this.impl = new EventGridPublisherClientImplBuilder().pipeline(pipeline).buildClient();

        this.endpoint = endpoint;
    }

    /**
     * Publishes the given EventGrid events to the set topic or domain.
     * @param events the EventGrid events to publish.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> publishEvents(Iterable<EventGridEvent> events) {
        // TODO: implement method
        return null;
    }

    Mono<Void> publishEvents(Iterable<EventGridEvent> events, Context context) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given cloud events to the set topic or domain.
     * @param events the cloud events to publish.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> publishCloudEvents(Iterable<CloudEvent> events) {
        // TODO: implement method
        return null;
    }

    Mono<Void> publishCloudEvents(Iterable<CloudEvent> events, Context context) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given custom events to the set topic or domain.
     * @param events the custom events to publish.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> publishCustomEvents(Iterable<Object> events) {
        // TODO: implement method
        return null;
    }

    Mono<Void> publishCustomEvents(Iterable<Object> events, Context context) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given EventGrid events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the EventGrid events to publish.
     *
     * @return the response from the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> publishEventsWithResponse(Iterable<EventGridEvent> events) {
        // TODO: implement method
        return null;
    }

    Mono<Response<Void>> publishEventsWithResponse(Iterable<EventGridEvent> events, Context context) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given cloud events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the cloud events to publish.
     *
     * @return the response from the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> publishCloudEventsWithResponse(Iterable<CloudEvent> events) {
        // TODO: implement method
        return null;
    }

    Mono<Response<Void>> publishCloudEventsWithResponse(Iterable<CloudEvent> events, Context context) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given custom events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the custom events to publish.
     *
     * @return the response from the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> publishCustomEventsWithResponse(Iterable<Object> events) {
        // TODO: implement method
        return null;
    }

    Mono<Response<Void>> publishCustomEventsWithResponse(Iterable<Object> events, Context context) {
        // TODO: implement method
        return null;
    }
}
