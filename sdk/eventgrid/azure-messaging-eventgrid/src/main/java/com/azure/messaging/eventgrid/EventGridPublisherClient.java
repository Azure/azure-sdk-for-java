// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.rest.Response;

import java.time.OffsetDateTime;

/**
 * A service client that publishes events to an EventGrid topic or domain. Use {@link EventGridPublisherClientBuilder}
 * to create an instance of this client. Note that this is simply a synchronous convenience layer over the
 * {@link EventGridPublisherAsyncClient}, which has more efficient asynchronous functionality and is recommended.
 * @see EventGridEvent
 * @see CloudEvent
 */
@ServiceClient(builder = EventGridPublisherClientBuilder.class)
public final class EventGridPublisherClient<T> {

    EventGridPublisherAsyncClient<T> asyncClient;
    EventGridPublisherClient(EventGridPublisherAsyncClient<T> client) {
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
     * Generate a shared access signature to provide time-limited authentication for requests to the Event Grid
     * service with the latest Event Grid service API defined in {@link EventGridServiceVersion#getLatest()}.
     * @param endpoint the endpoint of the Event Grid topic or domain.
     * @param expirationTime the time in which the signature should expire, no longer providing authentication.
     * @param keyCredential  the access key obtained from the Event Grid topic or domain.
     *
     * @return the shared access signature string which can be used to construct an instance of
     * {@link AzureSasCredential}.
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
     * @param keyCredential  the access key obtained from the Event Grid topic or domain.
     * @param apiVersion the EventGrid service api version defined in {@link EventGridServiceVersion}
     *
     * @return the shared access signature string which can be used to construct an instance of
     * {@link AzureSasCredential}.
     */
    public static String generateSas(String endpoint, AzureKeyCredential keyCredential, OffsetDateTime expirationTime,
        EventGridServiceVersion apiVersion) {
        return EventGridPublisherAsyncClient.generateSas(endpoint, keyCredential, expirationTime, apiVersion);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendEvents(Iterable<T> events) {
        asyncClient.sendEvents(events).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendEventsWithResponse(Iterable<T> events) {
        return asyncClient.sendEventsWithResponse(events).block();
    }
}
