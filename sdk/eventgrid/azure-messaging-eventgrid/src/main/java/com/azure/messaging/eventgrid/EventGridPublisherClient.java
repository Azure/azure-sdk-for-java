// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.models.CloudEvent;
import com.azure.core.util.Context;

import java.time.OffsetDateTime;

/**
 * A service client that publishes events to an EventGrid topic or domain. Use {@link EventGridPublisherClientBuilder}
 * to create an instance of this client. Note that this is simply a synchronous convenience layer over the
 * {@link EventGridPublisherAsyncClient}, which has more efficient asynchronous functionality and is recommended.
 *
 * <p><strong>Create EventGridPublisherClient for CloudEvent Samples</strong></p>
 * {@codesnippet com.azure.messaging.eventgrid.EventGridPublisherClient#CreateCloudEventClient}
 *
 * <p><strong>Send CloudEvent Samples</strong></p>
 * {@codesnippet com.azure.messaging.eventgrid.EventGridPublisherClient#SendCloudEvent}
 *
 * <p><strong>Create EventGridPublisherClient for EventGridEvent Samples</strong></p>
 * {@codesnippet com.azure.messaging.eventgrid.EventGridPublisherClient#CreateEventGridEventClient}
 *
 * <p><strong>Send EventGridEvent Samples</strong></p>
 * {@codesnippet com.azure.messaging.eventgrid.EventGridPublisherClient#SendEventGridEvent}
 *
 * <p><strong>Create EventGridPublisherClient for Custom Event Schema Samples</strong></p>
 * {@codesnippet com.azure.messaging.eventgrid.EventGridPublisherClient#CreateCustomEventClient}
 *
 * <p><strong>Send Custom Event Schema Samples</strong></p>
 * {@codesnippet com.azure.messaging.eventgrid.EventGridPublisherClient#SendCustomEvent}
 *
 * @see EventGridEvent
 * @see CloudEvent
 */
@ServiceClient(builder = EventGridPublisherClientBuilder.class)
public final class EventGridPublisherClient<T> {

    private final EventGridPublisherAsyncClient<T> asyncClient;
    EventGridPublisherClient(EventGridPublisherAsyncClient<T> client) {
        this.asyncClient = client;
    }

    /**
     * Generate a shared access signature to provide time-limited authentication for requests to the Event Grid
     * service with the latest Event Grid service API defined in {@link EventGridServiceVersion#getLatest()}.
     * @param endpoint the endpoint of the Event Grid topic or domain.
     * @param expirationTime the time in which the signature should expire, no longer providing authentication.
     * @param keyCredential the access key obtained from the Event Grid topic or domain.
     *
     * @return the shared access signature string which can be used to construct an instance of
     * {@link AzureSasCredential}.
     *
     * @throws NullPointerException if keyCredential or expirationTime is {@code null}.
     * @throws RuntimeException if java security doesn't have algorithm "hmacSHA256".
     */
    public static String generateSas(String endpoint, AzureKeyCredential keyCredential, OffsetDateTime expirationTime) {
        return EventGridPublisherAsyncClient.generateSas(endpoint, keyCredential, expirationTime,
            EventGridServiceVersion.getLatest());
    }

    /**
     * Generate a shared access signature to provide time-limited authentication for requests to the Event Grid
     * service.
     * @param endpoint the endpoint of the Event Grid topic or domain.
     * @param expirationTime the time in which the signature should expire, no longer providing authentication.
     * @param keyCredential the access key obtained from the Event Grid topic or domain.
     * @param apiVersion the EventGrid service api version defined in {@link EventGridServiceVersion}
     *
     * @return the shared access signature string which can be used to construct an instance of
     * {@link AzureSasCredential}.
     *
     * @throws NullPointerException if keyCredential or expirationTime is {@code null}.
     * @throws RuntimeException if java security doesn't have algorithm "hmacSHA256".
     */
    public static String generateSas(String endpoint, AzureKeyCredential keyCredential, OffsetDateTime expirationTime,
        EventGridServiceVersion apiVersion) {
        return EventGridPublisherAsyncClient.generateSas(endpoint, keyCredential, expirationTime, apiVersion);
    }

    /**
     * Publishes the given events to the given topic or domain.
     * @param events the cloud events to publish.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendEvents(Iterable<T> events) {
        asyncClient.sendEvents(events).block();
    }

    /**
     * Publishes the given events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the events to publish.
     * @param context the context to use along the pipeline.
     *
     * @return the response from the EventGrid service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendEventsWithResponse(Iterable<T> events, Context context) {
        return asyncClient.sendEventsWithResponse(events, context).block();
    }

    /**
     * Publishes the given event to the set topic or domain and gives the response issued by EventGrid.
     * @param event the event to publish.
     *
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendEvent(T event) {
        asyncClient.sendEvent(event).block();
    }
}
