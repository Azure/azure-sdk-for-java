// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.messaging.eventgrid.models.CloudEvent;
import com.azure.messaging.eventgrid.models.EventGridEvent;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * A service client that publishes events to an EventGrid topic or domain. Use {@link EventGridPublisherClientBuilder}
 * to create an instance of this client. This uses Project Reactor (https://projectreactor.io/) to handle asynchronous
 * programming.
 * @see EventGridEvent
 * @see CloudEvent
 */
@ServiceClient(builder = EventGridPublisherClientBuilder.class, isAsync = true)
public class EventGridPublisherAsyncClient {

    EventGridPublisherAsyncClient(HttpPipeline pipeline, String endpoint) {
        // TODO: implement method
    }

    /**
     * Publishes the given EventGrid events to the set topic or domain.
     * @param events the EventGrid events to publish.
     *
     * @return the completion.
     */
    public Mono<Void> publishEvents(List<EventGridEvent> events) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given cloud events to the set topic or domain.
     * @param events the cloud events to publish.
     *
     * @return the completion.
     */
    public Mono<Void> publishCloudEvents(List<CloudEvent> events) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given custom events to the set topic or domain.
     * @param events the custom events to publish.
     *
     * @return the completion.
     */
    public Mono<Void> publishCustomEvents(List<Object> events) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given EventGrid events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the EventGrid events to publish.
     *
     * @return the response from the EventGrid service.
     */
    public Mono<Response<Void>> publishEventsWithResponse(List<EventGridEvent> events) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given cloud events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the cloud events to publish.
     *
     * @return the response from the EventGrid service.
     */
    public Mono<Response<Void>> publishCloudEventsWithResponse(List<CloudEvent> events) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given custom events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the custom events to publish.
     *
     * @return the response from the EventGrid service.
     */
    public Mono<Response<Void>> publishCustomEventsWithResponse(List<Object> events) {
        // TODO: implement method
        return null;
    }
}
