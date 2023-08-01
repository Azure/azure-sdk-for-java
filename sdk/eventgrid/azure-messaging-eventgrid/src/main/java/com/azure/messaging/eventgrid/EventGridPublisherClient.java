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
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherClient#CreateCloudEventClient -->
 * <pre>
 * &#47;&#47; Create a client to send events of CloudEvent schema &#40;com.azure.core.models.CloudEvent&#41;
 * EventGridPublisherClient&lt;CloudEvent&gt; cloudEventPublisherClient = new EventGridPublisherClientBuilder&#40;&#41;
 *     .endpoint&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT&quot;&#41;&#41;  &#47;&#47; make sure it accepts CloudEvent
 *     .credential&#40;new AzureKeyCredential&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_CLOUDEVENT_KEY&quot;&#41;&#41;&#41;
 *     .buildCloudEventPublisherClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherClient#CreateCloudEventClient -->
 *
 * <p><strong>Send CloudEvent Samples</strong></p>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherClient#SendCloudEvent -->
 * <pre>
 * &#47;&#47; Create a com.azure.models.CloudEvent.
 * User user = new User&#40;&quot;Stephen&quot;, &quot;James&quot;&#41;;
 * CloudEvent cloudEventDataObject = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
 *     BinaryData.fromObject&#40;user&#41;, CloudEventDataFormat.JSON, &quot;application&#47;json&quot;&#41;;
 *
 * &#47;&#47; Send a single CloudEvent
 * cloudEventPublisherClient.sendEvent&#40;cloudEventDataObject&#41;;
 *
 * &#47;&#47; Send a list of CloudEvents to the EventGrid service altogether.
 * &#47;&#47; This has better performance than sending one by one.
 * cloudEventPublisherClient.sendEvents&#40;Arrays.asList&#40;
 *     cloudEventDataObject
 *     &#47;&#47; add more CloudEvents objects
 * &#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherClient#SendCloudEvent -->
 *
 * <p><strong>Create EventGridPublisherClient for EventGridEvent Samples</strong></p>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherClient#CreateEventGridEventClient -->
 * <pre>
 * &#47;&#47; Create a client to send events of EventGridEvent schema
 * EventGridPublisherClient&lt;EventGridEvent&gt; eventGridEventPublisherClient = new EventGridPublisherClientBuilder&#40;&#41;
 *     .endpoint&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_EVENT_ENDPOINT&quot;&#41;&#41;  &#47;&#47; make sure it accepts EventGridEvent
 *     .credential&#40;new AzureKeyCredential&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_EVENT_KEY&quot;&#41;&#41;&#41;
 *     .buildEventGridEventPublisherClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherClient#CreateEventGridEventClient -->
 *
 * <p><strong>Send EventGridEvent Samples</strong></p>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherClient#SendEventGridEvent -->
 * <pre>
 * &#47;&#47; Create an EventGridEvent
 * User user = new User&#40;&quot;John&quot;, &quot;James&quot;&#41;;
 * EventGridEvent eventGridEvent = new EventGridEvent&#40;&quot;&#47;EventGridEvents&#47;example&#47;source&quot;,
 *     &quot;Example.EventType&quot;, BinaryData.fromObject&#40;user&#41;, &quot;0.1&quot;&#41;;
 *
 * &#47;&#47; Send a single EventGridEvent
 * eventGridEventPublisherClient.sendEvent&#40;eventGridEvent&#41;;
 *
 * &#47;&#47; Send a list of EventGridEvents to the EventGrid service altogether.
 * &#47;&#47; This has better performance than sending one by one.
 * eventGridEventPublisherClient.sendEvents&#40;Arrays.asList&#40;
 *     eventGridEvent
 *     &#47;&#47; add more EventGridEvents objects
 * &#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherClient#SendEventGridEvent -->
 *
 * <p><strong>Create EventGridPublisherClient for Custom Event Schema Samples</strong></p>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherClient#CreateCustomEventClient -->
 * <pre>
 * &#47;&#47; Create a client to send events of custom event
 * EventGridPublisherClient&lt;BinaryData&gt; customEventPublisherClient = new EventGridPublisherClientBuilder&#40;&#41;
 *     .endpoint&#40;System.getenv&#40;&quot;AZURE_CUSTOM_EVENT_ENDPOINT&quot;&#41;&#41;  &#47;&#47; make sure it accepts custom events
 *     .credential&#40;new AzureKeyCredential&#40;System.getenv&#40;&quot;AZURE_CUSTOM_EVENT_KEY&quot;&#41;&#41;&#41;
 *     .buildCustomEventPublisherClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherClient#CreateCustomEventClient -->
 *
 * <p><strong>Send Custom Event Schema Samples</strong></p>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherClient#SendCustomEvent -->
 * <pre>
 * &#47;&#47; Create an custom event object
 * Map&lt;String, Object&gt; customEvent = new HashMap&lt;String, Object&gt;&#40;&#41; &#123;
 *     &#123;
 *         put&#40;&quot;id&quot;, UUID.randomUUID&#40;&#41;.toString&#40;&#41;&#41;;
 *         put&#40;&quot;subject&quot;, &quot;Test&quot;&#41;;
 *         put&#40;&quot;foo&quot;, &quot;bar&quot;&#41;;
 *         put&#40;&quot;type&quot;, &quot;Microsoft.MockPublisher.TestEvent&quot;&#41;;
 *         put&#40;&quot;data&quot;, 100.0&#41;;
 *         put&#40;&quot;dataVersion&quot;, &quot;0.1&quot;&#41;;
 *     &#125;
 * &#125;;
 *
 * &#47;&#47; Send a single custom event
 * customEventPublisherClient.sendEvent&#40;BinaryData.fromObject&#40;customEvent&#41;&#41;;
 *
 * &#47;&#47; Send a list of custom events to the EventGrid service altogether.
 * &#47;&#47; This has better performance than sending one by one.
 * customEventPublisherClient.sendEvents&#40;Arrays.asList&#40;
 *     BinaryData.fromObject&#40;customEvent&#41;
 *     &#47;&#47; add more custom events in BinaryData
 * &#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherClient#SendCustomEvent -->
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
     * Publishes the given events to the set topic or domain and gives the response issued by EventGrid.
     * @param events the events to publish.
     * @param channelName the channel name to send to Event Grid service. This is only applicable for sending
     *   Cloud Events to a partner topic in partner namespace. For more details, refer to
     *   <a href=https://docs.microsoft.com/azure/event-grid/partner-events-overview>Partner Events Overview.</a>
     * @param context the context to use along the pipeline.
     *
     * @return the response from the EventGrid service.
     * @throws NullPointerException if events is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendEventsWithResponse(Iterable<T> events, String channelName,
                                                 Context context) {
        return asyncClient.sendEventsWithResponse(events, channelName, context).block();
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
