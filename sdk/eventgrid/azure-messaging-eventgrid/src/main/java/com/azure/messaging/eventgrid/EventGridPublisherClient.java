// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.messaging.eventgrid.implementation.EventGridPublisherClientImpl;
import com.azure.messaging.eventgrid.models.CloudEvent;
import com.azure.messaging.eventgrid.models.EventGridEvent;

import java.util.List;

/**
 * A service client that publishes events to an EventGrid topic or domain. Use {@link EventGridPublisherClientBuilder}
 * to create an instance of this client. Note that this is simply a synchronous convenience layer over the
 * {@link EventGridPublisherAsyncClient}, which has more efficient asynchronous functionality and is recommended.
 * @see EventGridEvent
 * @see CloudEvent
 */
@ServiceClient(builder = EventGridPublisherClientBuilder.class)
public class EventGridPublisherClient {

    EventGridPublisherClient(HttpPipeline pipeline, String endpoint) {
        // TODO: implement method
    }

    /**
     * Publishes the given EventGrid events to the given topic or domain.
     * @param events the EventGrid events to publish.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void publishEvents(List<EventGridEvent> events) {
        // TODO: implement method
    }

    /**
     * Publishes the given cloud events to the given topic or domain.
     * @param events the cloud events to publish.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void publishCloudEvents(List<CloudEvent> events) {
        // TODO: implement method
    }

    /**
     * Publishes the given custom events to the given topic or domain.
     * @param events the custom events to publish.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void publishCustomEvents(List<Object> events) {
        // TODO: implement method
    }

    /**
     * Publishes the given EventGrid events to the given topic or domain and gives the response issued by EventGrid.
     * @param events the EventGrid events to publish.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> publishEventsWithResponse(List<EventGridEvent> events) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given cloud events to the given topic or domain and gives the response issued by EventGrid.
     * @param events the cloud events to publish.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> publishCloudEventsWithResponse(List<CloudEvent> events) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given custom events to the given topic or domain and gives the response issued by EventGrid.
     * @param events the custom events to publish.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> publishCustomEventsWithResponse(List<Object> events) {
        // TODO: implement method
        return null;
    }
}
