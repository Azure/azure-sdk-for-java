// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * A service client that publishes events to an EventGrid topic or domain. Use {@link EventGridPublisherClientBuilder}
 * to create an instance of this client. Note that this is simply a synchronous convenience layer over the
 * {@link EventGridPublisherAsyncClient}, which has more efficient asynchronous functionality and is recommended.
 * @see EventGridEvent
 * @see CloudEvent
 */
@ServiceClient(builder = EventGridPublisherClientBuilder.class)
public final class EventGridPublisherClient {

    EventGridPublisherAsyncClient asyncClient;
    EventGridPublisherClient(EventGridPublisherAsyncClient client) {
        this.asyncClient = client;
    }

    /**
     * Get the service version of the Rest API.
     * @return the Service version of the rest API
     */
    public EventGridServiceVersion getServiceVersion() {
        return asyncClient.getServiceVersion();
    }

    /**
     * Publishes the given EventGrid events to the given topic or domain.
     * @param events the EventGrid events to publish.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendEventGridEvents(Iterable<EventGridEvent> events) {
        asyncClient.sendEventGridEvents(events, Context.NONE).block();
    }

    /**
     * Publishes the given cloud events to the given topic or domain.
     * @param events the cloud events to publish.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendCloudEvents(Iterable<CloudEvent> events) {
        asyncClient.sendCloudEvents(events, Context.NONE).block();
    }

    /**
     * Publishes the given custom events to the given topic or domain.
     * @param events the custom events to publish.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendCustomEvents(Iterable<Object> events) {
        asyncClient.sendCustomEvents(events, Context.NONE).block();
    }

    /**
     * Publishes the given EventGrid events to the given topic or domain and gives the response issued by EventGrid.
     * @param events  the EventGrid events to publish.
     * @param context the context to use along the pipeline.
     *
     * @return the response given by the EventGrid service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendEventGridEventsWithResponse(Iterable<EventGridEvent> events, Context context) {
        return asyncClient.sendEventGridEventsWithResponse(events, context).block();
    }

    /**
     * Publishes the given cloud events to the given topic or domain and gives the response issued by EventGrid.
     * @param events  the cloud events to publish.
     * @param context the context to use along the pipeline.
     *
     * @return the response given by the EventGrid service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendCloudEventsWithResponse(Iterable<CloudEvent> events, Context context) {
        return asyncClient.sendCloudEventsWithResponse(events, context).block();
    }

    /**
     * Publishes the given custom events to the given topic or domain and gives the response issued by EventGrid.
     * @param events  the custom events to publish.
     * @param context the context to use along the pipeline.
     *
     * @return the response given by the EventGrid service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendCustomEventsWithResponse(Iterable<Object> events, Context context) {
        return asyncClient.sendCustomEventsWithResponse(events, context).block();
    }
}
