// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/en-us/azure/event-grid/">Azure Event Grid</a> is a highly scalable, fully
 * managed event routing service. With Event Grid can connect applications and services to react to relevant events.
 * This library is for publishing Event Grid events and deserializing event payloads in subscriptions.</p>
 *
 * <p><strong>Key Concepts:</strong></p>
 * <ul>
 *     <li><strong>Events</strong> - Information about what happened.</li>
 *     <li><strong>Event Source</strong> - where the event took place.</li>
 *     <li><strong>Topics</strong> - the endpoint where events are published to.</li>
 *     <li><strong>Event Handlers</strong> - the endpoint that handles the events.</li>
 *     <li><strong>Event Subscriptions</strong> - the endpoint or built-in mechanism for routing events.</li>
 * </ul>
 * <p>For more information see the <a href="https://learn.microsoft.com/en-us/azure/event-grid/concepts">concepts overview.</a></p>
 * <h2>Send an EventGridEvent</h2>
 * <p>In order to interact with the Azure Event Grid service, you will need to create an instance of the {@link com.azure.messaging.eventgrid.EventGridPublisherClient} class:</p>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherClient#CreateEventGridEventClient -->
 * <pre>
 * &#47;&#47; Create a client to send events of EventGridEvent schema
 * EventGridPublisherClient&lt;EventGridEvent&gt; eventGridEventPublisherClient = new EventGridPublisherClientBuilder&#40;&#41;
 *     .endpoint&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_EVENT_ENDPOINT&quot;&#41;&#41;  &#47;&#47; make sure it accepts EventGridEvent
 *     .credential&#40;new AzureKeyCredential&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_EVENT_KEY&quot;&#41;&#41;&#41;
 *     .buildEventGridEventPublisherClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherClient#CreateEventGridEventClient -->
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendEventGridEvent -->
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendEventGridEvent -->
 *
 * <h2>Send a Cloud Event</h2>
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateCloudEventClient -->
 * <pre>
 * &#47;&#47; Create a client to send events of CloudEvent schema &#40;com.azure.core.models.CloudEvent&#41;
 * EventGridPublisherAsyncClient&lt;CloudEvent&gt; cloudEventPublisherClient = new EventGridPublisherClientBuilder&#40;&#41;
 *     .endpoint&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT&quot;&#41;&#41;  &#47;&#47; make sure it accepts CloudEvent
 *     .credential&#40;new AzureKeyCredential&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_CLOUDEVENT_KEY&quot;&#41;&#41;&#41;
 *     .buildCloudEventPublisherAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#CreateCloudEventClient -->
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendCloudEvent -->
 * <pre>
 * &#47;&#47; Create a com.azure.models.CloudEvent.
 * User user = new User&#40;&quot;Stephen&quot;, &quot;James&quot;&#41;;
 * CloudEvent cloudEventDataObject = new CloudEvent&#40;&quot;&#47;cloudevents&#47;example&#47;source&quot;, &quot;Example.EventType&quot;,
 *     BinaryData.fromObject&#40;user&#41;, CloudEventDataFormat.JSON, &quot;application&#47;json&quot;&#41;;
 *
 * &#47;&#47; Send a single CloudEvent
 * cloudEventPublisherClient.sendEvent&#40;cloudEventDataObject&#41;.block&#40;&#41;;
 *
 * &#47;&#47; Send a list of CloudEvents to the EventGrid service altogether.
 * &#47;&#47; This has better performance than sending one by one.
 * cloudEventPublisherClient.sendEvents&#40;Arrays.asList&#40;
 *     cloudEventDataObject
 *     &#47;&#47; add more CloudEvents objects
 * &#41;&#41;.block&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendCloudEvent -->
 */
package com.azure.messaging.eventgrid;
