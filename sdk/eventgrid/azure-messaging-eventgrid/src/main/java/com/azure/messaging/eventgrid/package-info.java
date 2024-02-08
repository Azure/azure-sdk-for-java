// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/en-us/azure/event-grid/">Azure Event Grid</a> is a highly scalable, fully
 * managed event routing service. With Event Grid can connect applications and services to react to relevant events.
 * This library is for publishing Event Grid events and deserializing event payloads in subscriptions.</p>
 *
 * <p><strong>Key Concepts:</strong></p>
 * <ul>
 *     <li><strong>Event</strong> - Information about what happened.</li>
 *     <li><strong>Event Source</strong> - where the event took place.</li>
 *     <li><strong>Topic</strong> - the endpoint where events are published to.</li>
 *     <li><strong>Event Handler</strong> - the endpoint that handles the events.</li>
 *     <li><strong>Event Subscription</strong> - the endpoint or built-in mechanism for routing events.</li>
 * </ul>
 *
 * <p>For more information see the <a href="https://learn.microsoft.com/en-us/azure/event-grid/concepts">concepts overview.</a></p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>The Azure EventGrid SDK provides {@link com.azure.messaging.eventgrid.EventGridPublisherClient} and
 * {@link com.azure.messaging.eventgrid.EventGridPublisherAsyncClient} for synchronous and asynchronous publishing of
 * events to Azure Event Grid. These can be instantiated using the {@link com.azure.messaging.eventgrid.EventGridPublisherClientBuilder}.</p>
 *
 * <h3>Authentication</h3>
 *
 * There are three ways to authenticate a publisher client for Azure Event Grid.
 *
 * <p><strong>Microsoft Entra ID</strong>: Using managed identity is the recommended way to authenticate. The recommended way to do so is using
 * DefaultAzureCredential:
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherClientBuilder#buildEventGridEventPublisherClientWithDac -->
 * <pre>
 * DefaultAzureCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * EventGridPublisherClient&lt;EventGridEvent&gt; eventGridEventPublisherClient = new EventGridPublisherClientBuilder&#40;&#41;
 *     .endpoint&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_EVENT_ENDPOINT&quot;&#41;&#41;
 *     .credential&#40;credential&#41;
 *     .buildEventGridEventPublisherClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherClientBuilder#buildEventGridEventPublisherClientWithDac -->
 *
 * <p><strong>Topic Access Key</strong>: When a topic is created, an access key is created for that topic. It is used
 * with the AzureKeyCredential:
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherClientBuilder#buildEventGridEventPublisherClientWithKey -->
 * <pre>
 * AzureKeyCredential credential = new AzureKeyCredential&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_EVENT_TOPIC_KEY&quot;&#41;&#41;;
 * EventGridPublisherClient&lt;EventGridEvent&gt; eventGridEventPublisherClient = new EventGridPublisherClientBuilder&#40;&#41;
 *     .endpoint&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_EVENT_ENDPOINT&quot;&#41;&#41;
 *     .credential&#40;credential&#41;
 *     .buildEventGridEventPublisherClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherClientBuilder#buildEventGridEventPublisherClientWithKey -->
 *
 * <p><strong>Shared Access Signature</strong>: A Shared Access Signature (SAS) key can be used to authenticate. First, you must create one:
 * <!-- src_embed com.azure.messaging.eventgrid.CreateSasToken -->
 * <pre>
 * &#47;&#47; You can get a SAS token using static methods of EventGridPublisherClient.
 * String sasKey = EventGridPublisherClient.generateSas&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_EVENT_ENDPOINT&quot;&#41;,
 *         new AzureKeyCredential&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_EVENT_TOPIC_KEY&quot;&#41;&#41;,
 *         OffsetDateTime.now&#40;&#41;.plusHours&#40;1&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.CreateSasToken -->
 *
 * Once it is created, it is used with a SAS token credential:
 * <!-- src_embed com.azure.messaging.eventgrid.EventGridPublisherClientBuilder#buildEventGridEventPublisherClientWithSas -->
 * <pre>
 * &#47;&#47; Once you have this key, you can share it with anyone who needs to send events to your topic. They use it like this:
 * AzureSasCredential credential = new AzureSasCredential&#40;sasKey&#41;;
 * EventGridPublisherClient&lt;EventGridEvent&gt; eventGridEventPublisherClient = new EventGridPublisherClientBuilder&#40;&#41;
 *     .endpoint&#40;System.getenv&#40;&quot;AZURE_EVENTGRID_EVENT_ENDPOINT&quot;&#41;&#41;
 *     .credential&#40;credential&#41;
 *     .buildEventGridEventPublisherClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherClientBuilder#buildEventGridEventPublisherClientWithSas -->
 *
 * <hr>
 * <h2>Send an EventGridEvent</h2>
 *
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
 * <pre>
 * &#47;&#47; Create an EventGridEvent
 * User user = new User&#40;&quot;John&quot;, &quot;James&quot;&#41;;
 * EventGridEvent eventGridEvent = new EventGridEvent&#40;&quot;&#47;EventGridEvents&#47;example&#47;source&quot;,
 *     &quot;Example.EventType&quot;, BinaryData.fromObject&#40;user&#41;, &quot;0.1&quot;&#41;;
 *
 * &#47;&#47; Send a single EventGridEvent
 * eventGridEventPublisherClient.sendEvent&#40;eventGridEvent&#41;.block&#40;&#41;;
 *
 * &#47;&#47; Send a list of EventGridEvents to the EventGrid service altogether.
 * &#47;&#47; This has better performance than sending one by one.
 * eventGridEventPublisherClient.sendEvents&#40;Arrays.asList&#40;
 *     eventGridEvent
 *     &#47;&#47; add more EventGridEvents objects
 * &#41;&#41;.block&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventgrid.EventGridPublisherAsyncClient#SendEventGridEvent -->
 *
 * <hr>
 * <h2>Send a Cloud Event</h2>
 *
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
 *
 * @see com.azure.messaging.eventgrid.EventGridPublisherClient
 * @see com.azure.messaging.eventgrid.EventGridPublisherAsyncClient
 * @see com.azure.messaging.eventgrid.EventGridPublisherClientBuilder
 */
package com.azure.messaging.eventgrid;
