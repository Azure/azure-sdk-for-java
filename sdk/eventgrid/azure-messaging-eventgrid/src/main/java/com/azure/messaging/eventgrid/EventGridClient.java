package com.azure.messaging.eventgrid;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.messaging.eventgrid.models.CloudEvent;
import com.azure.messaging.eventgrid.models.EventGridEvent;

import java.util.List;

import reactor.core.publisher.Mono;

/** The interface for EventGridClient class. */
public interface EventGridClient {
    /**
     * Gets Api Version.
     * @return the apiVersion value.
     */
    String getApiVersion();

    /**
     * Gets The HTTP pipeline to send requests through.
     * @return the httpPipeline value.
     */
    HttpPipeline getHttpPipeline();

    /**
     * Publishes a batch of events to an Azure Event Grid topic.
     * @param topicHostname simple string.
     * @param events        Array of EventGridEvent.
     *
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException    thrown if the request is rejected by server.
     * @throws RuntimeException         all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<Void>> publishEventsWithResponseAsync(String topicHostname, List<EventGridEvent> events);

    /**
     * Publishes a batch of events to an Azure Event Grid topic.
     * @param topicHostname simple string.
     * @param events        Array of EventGridEvent.
     *
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException    thrown if the request is rejected by server.
     * @throws RuntimeException         all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Void> publishEventsAsync(String topicHostname, List<EventGridEvent> events);

    /**
     * Publishes a batch of events to an Azure Event Grid topic.
     * @param topicHostname simple string.
     * @param events        Array of CloudEventEvent.
     *
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException    thrown if the request is rejected by server.
     * @throws RuntimeException         all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<Void>> publishCloudEventEventsWithResponseAsync(String topicHostname, List<CloudEvent> events);

    /**
     * Publishes a batch of events to an Azure Event Grid topic.
     * @param topicHostname simple string.
     * @param events        Array of CloudEventEvent.
     *
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException    thrown if the request is rejected by server.
     * @throws RuntimeException         all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Void> publishCloudEventEventsAsync(String topicHostname, List<CloudEvent> events);

    /**
     * Publishes a batch of events to an Azure Event Grid topic.
     * @param topicHostname simple string.
     * @param events        Array of any.
     *
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException    thrown if the request is rejected by server.
     * @throws RuntimeException         all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<Void>> publishCustomEventEventsWithResponseAsync(String topicHostname, List<Object> events);

    /**
     * Publishes a batch of events to an Azure Event Grid topic.
     * @param topicHostname simple string.
     * @param events        Array of any.
     *
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException    thrown if the request is rejected by server.
     * @throws RuntimeException         all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Void> publishCustomEventEventsAsync(String topicHostname, List<Object> events);
}
