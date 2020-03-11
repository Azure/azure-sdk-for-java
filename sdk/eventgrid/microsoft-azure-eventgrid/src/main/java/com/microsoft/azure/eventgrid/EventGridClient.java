// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.eventgrid;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.eventgrid.models.EventGridEvent;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;

import java.util.List;

/**
 * The interface for EventGridClient class.
 */
public interface EventGridClient {
    /**
     * Gets the REST client.
     *
     * @return the {@link RestClient} object.
    */
    RestClient restClient();

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    AzureClient getAzureClient();

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    String userAgent();

    /**
     * Gets Version of the API to be used with the client request..
     *
     * @return the apiVersion value.
     */
    String apiVersion();

    /**
     * Gets Gets or sets the preferred language for the response..
     *
     * @return the acceptLanguage value.
     */
    String acceptLanguage();

    /**
     * Sets Gets or sets the preferred language for the response..
     *
     * @param acceptLanguage the acceptLanguage value.
     * @return the service client itself
     */
    EventGridClient withAcceptLanguage(String acceptLanguage);

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30..
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    int longRunningOperationRetryTimeout();

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30..
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     * @return the service client itself
     */
    EventGridClient withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout);

    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true..
     *
     * @return the generateClientRequestId value.
     */
    boolean generateClientRequestId();

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true..
     *
     * @param generateClientRequestId the generateClientRequestId value.
     * @return the service client itself
     */
    EventGridClient withGenerateClientRequestId(boolean generateClientRequestId);

    /**
     * Publishes a batch of events to an Azure Event Grid topic.
     *
     * @param topicHostname The host name of the topic, e.g. topic1.westus2-1.eventgrid.azure.net
     * @param events An array of events to be published to Event Grid.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void publishEvents(String topicHostname, List<EventGridEvent> events);

    /**
     * Publishes a batch of events to an Azure Event Grid topic.
     *
     * @param topicHostname The host name of the topic, e.g. topic1.westus2-1.eventgrid.azure.net
     * @param events An array of events to be published to Event Grid.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<Void> publishEventsAsync(String topicHostname, List<EventGridEvent> events, ServiceCallback<Void> serviceCallback);

    /**
     * Publishes a batch of events to an Azure Event Grid topic.
     *
     * @param topicHostname The host name of the topic, e.g. topic1.westus2-1.eventgrid.azure.net
     * @param events An array of events to be published to Event Grid.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<Void> publishEventsAsync(String topicHostname, List<EventGridEvent> events);

    /**
     * Publishes a batch of events to an Azure Event Grid topic.
     *
     * @param topicHostname The host name of the topic, e.g. topic1.westus2-1.eventgrid.azure.net
     * @param events An array of events to be published to Event Grid.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    Observable<ServiceResponse<Void>> publishEventsWithServiceResponseAsync(String topicHostname, List<EventGridEvent> events);

}
