// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

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
 * @see EventGridEventBuilder
 * @see CloudEventBuilder
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
        asyncClient.publishEvents(events, Context.NONE).block();
    }

    /**
     * Publishes the given cloud events to the given topic or domain.
     * @param events the cloud events to publish.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendCloudEvents(Iterable<CloudEvent> events) {
        asyncClient.publishCloudEvents(events, Context.NONE).block();
    }

    /**
     * Publishes the given custom events to the given topic or domain.
     * @param events the custom events to publish.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendCustomEvents(Iterable<Object> events) {
        asyncClient.publishCustomEvents(events, Context.NONE).block();
    }

    /**
     * Publishes the given EventGrid events to the given topic or domain and gives the response issued by EventGrid.
     * @param events the EventGrid events to publish.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendEventsWithResponse(Iterable<EventGridEvent> events) {
        return asyncClient.publishEventsWithResponse(events, Context.NONE).block();
    }

    /**
     * Publishes the given cloud events to the given topic or domain and gives the response issued by EventGrid.
     * @param events the cloud events to publish.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendCloudEventsWithResponse(Iterable<CloudEvent> events) {
        return asyncClient.publishCloudEventsWithResponse(events, Context.NONE).block();
    }

    /**
     * Publishes the given custom events to the given topic or domain and gives the response issued by EventGrid.
     * @param events the custom events to publish.
     *
     * @return the response given by the EventGrid service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendCustomEventsWithResponse(Iterable<Object> events) {
        return asyncClient.publishCustomEventsWithResponse(events, Context.NONE).block();
    }

    /**
     * Publishes the given EventGrid events to the given topic or domain.
     * @param events  the EventGrid events to publish.
     * @param context the context to use along the pipeline.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendEvents(Iterable<EventGridEvent> events, Context context) {
        asyncClient.publishEvents(events, context).block();
    }

    /**
     * Publishes the given cloud events to the given topic or domain.
     * @param events  the cloud events to publish.
     * @param context the context to use along the pipeline.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendCloudEvents(Iterable<CloudEvent> events, Context context) {
        asyncClient.publishCloudEvents(events, context).block();
    }

    /**
     * Publishes the given custom events to the given topic or domain.
     * @param events  the custom events to publish.
     * @param context the context to use along the pipeline.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendCustomEvents(Iterable<Object> events, Context context) {
        asyncClient.publishCustomEvents(events, context).block();
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
        return asyncClient.publishEventsWithResponse(events, context).block();
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
        return asyncClient.publishCloudEventsWithResponse(events, context).block();
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
        return asyncClient.publishCustomEventsWithResponse(events, context).block();
    }
}
