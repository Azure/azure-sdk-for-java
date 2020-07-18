// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.messaging.eventgrid.events.CloudEvent;
import com.azure.messaging.eventgrid.events.EventGridEvent;

/**
 * A service client that publishes events to an EventGrid topic or domain. Use {@link EventGridPublisherClientBuilder}
 * to create an instance of this client. Note that this is simply a synchronous convenience layer over the
 * {@link EventGridPublisherAsyncClient}, which has more efficient asynchronous functionality and is recommended.
 * @see com.azure.messaging.eventgrid.events.EventGridEventBuilder
 * @see com.azure.messaging.eventgrid.events.CloudEventBuilder
 */
@ServiceClient(builder = EventGridPublisherClientBuilder.class)
public class EventGridPublisherClient {

    EventGridPublisherAsyncClient asyncClient;

    EventGridPublisherClient(EventGridPublisherAsyncClient client) {
        this.asyncClient = client;
    }

    /**
     * Publishes the given EventGrid events to the given topic or domain.
     * @param events the EventGrid events to publish.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendEvents(Iterable<EventGridEvent> events) {
        // TODO: implement method
    }

    /**
     * Publishes the given cloud events to the given topic or domain.
     * @param events the cloud events to publish.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendCloudEvents(Iterable<CloudEvent> events) {
        // TODO: implement method
    }

    /**
     * Publishes the given custom events to the given topic or domain.
     * @param events the custom events to publish.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendCustomEvents(Iterable<Object> events) {
        // TODO: implement method
    }

    /**
     * Publishes the given EventGrid events to the given topic or domain and gives the response issued by EventGrid.
     * @param events the EventGrid events to publish.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendEventsWithResponse(Iterable<EventGridEvent> events) {
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
    public Response<Void> sendCloudEventsWithResponse(Iterable<CloudEvent> events) {
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
    public Response<Void> sendCustomEventsWithResponse(Iterable<Object> events) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given EventGrid events to the given topic or domain.
     * @param events  the EventGrid events to publish.
     * @param context the context to use along the pipeline.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendEvents(Iterable<EventGridEvent> events, Context context) {
        // TODO: implement method
    }

    /**
     * Publishes the given cloud events to the given topic or domain.
     * @param events  the cloud events to publish.
     * @param context the context to use along the pipeline.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendCloudEvents(Iterable<CloudEvent> events, Context context) {
        // TODO: implement method
    }

    /**
     * Publishes the given custom events to the given topic or domain.
     * @param events  the custom events to publish.
     * @param context the context to use along the pipeline.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendCustomEvents(Iterable<Object> events, Context context) {
        // TODO: implement method
    }

    /**
     * Publishes the given EventGrid events to the given topic or domain and gives the response issued by EventGrid.
     * @param events  the EventGrid events to publish.
     * @param context the context to use along the pipeline.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendEventsWithResponse(Iterable<EventGridEvent> events, Context context) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given cloud events to the given topic or domain and gives the response issued by EventGrid.
     * @param events  the cloud events to publish.
     * @param context the context to use along the pipeline.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendCloudEventsWithResponse(Iterable<CloudEvent> events, Context context) {
        // TODO: implement method
        return null;
    }

    /**
     * Publishes the given custom events to the given topic or domain and gives the response issued by EventGrid.
     * @param events  the custom events to publish.
     * @param context the context to use along the pipeline.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendCustomEventsWithResponse(Iterable<Object> events, Context context) {
        // TODO: implement method
        return null;
    }
}
