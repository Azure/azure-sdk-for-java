// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/azure/event-hubs/event-hubs-about">Azure Event Hubs</a> is a highly scalable
 * publish-subscribe service that can ingest millions of events per second and stream them to multiple consumers. This
 * lets you process and analyze the massive amounts of data produced by your connected devices and applications. Once
 * Event Hubs has collected the data, you can retrieve, transform, and store it by using any real-time analytics
 * provider or with batching/storage adapters.</p>
 *
 * <p>The Azure Event Hubs client library allows Java developers to interact with Azure Event Hubs. It provides a set
 * of clients that enable Java developers to publish events to and consume events from an Event Hub.</p>
 *
 * <h2>Key Concepts</h2>
 *
 * <ul>
 *      <li><strong>Event Hub producer:</strong>  A source of telemetry data, diagnostics information, usage logs, or
 *      other data, as part of an embedded device solution, a mobile device application, a game title running on a
 *      console or other device, some client or server based business solution, or a website</li>
 *
 *      <li><strong>Event Hub consumer:</strong>  Fetches events published to an Event Hub and processes it.  Processing
 *      may involve aggregation, complex computation, and filtering. Processing may also involve distribution or storage
 *      of the information in a raw or transformed fashion. Event Hub consumers are often robust and high-scale platform
 *      infrastructure parts with built-in analytics capabilities, like Azure Stream Analytics, Apache Spark, or Apache
 *      Storm.</li>
 *
 *      <li><strong>Partition:</strong>  An ordered sequence of events that is held in an Event Hub. Azure Event Hubs
 *      provides message streaming through a partitioned consumer pattern in which each consumer only reads a specific
 *      subset, or partition, of the message stream. As newer events arrive, they are added to the end of this sequence.
 *      The number of partitions is specified at the time an Event Hub is created and cannot be changed.</li>
 *
 *      <li><strong>Consumer group:</strong>  A view of an entire Event Hub. Consumer groups enable multiple consuming
 *      applications to each have a separate view of the event stream, and to read the stream independently at their own
 *      pace and from their own position. There can be at most 5 concurrent readers on a partition per consumer group;
 *      however it is recommended that there is only one active consumer for a given partition and consumer group
 *      pairing. Each active reader receives the events from its partition; if there are multiple readers on the same
 *      partition, then they will receive duplicate events.</li>
 *
 *      <li><strong>Stream offset:</strong>  The position of an event within an Event Hub partition.  It is a
 *      client-side cursor that specifies the point in the stream where the event is located.  The offset of an
 *      event can change as events expire from the stream.</li>
 *
 *      <li><strong>Stream sequence number:</strong> A number assigned to the event when it was enqueued in the
 *      associated Event Hub partition. This is unique for every message received in the Event Hub partition.</li>
 *
 *      <li><strong>Checkpointing:</strong>  A process by which readers mark or commit their position within a
 *      partition event sequence. Checkpointing is the responsibility of the consumer and occurs on a per-partition
 *      basis within a consumer group. This responsibility means that for each consumer group, each partition reader
 *      must keep track of its current position in the event stream, and can inform the service when it considers the
 *      data stream complete.</li>
 * </ul>
 *
 * <h2>Getting Started</h2>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Event Hubs.
 * {@link com.azure.messaging.eventhubs.EventHubProducerClient} and
 * {@link com.azure.messaging.eventhubs.EventHubProducerAsyncClient} are the sync and async clients for publishing
 * events to an Event Hub.  Similarly, {@link com.azure.messaging.eventhubs.EventHubConsumerClient} and
 * {@link com.azure.messaging.eventhubs.EventHubConsumerAsyncClient} are the sync and async clients for consuming
 * events from an Event Hub.  In production scenarios, we recommend users leverage
 * {@link com.azure.messaging.eventhubs.EventProcessorClient} because consumes events from all Event Hub partition, load
 * balances work between multiple instances of {@link com.azure.messaging.eventhubs.EventProcessorClient} and can
 * perform checkpointing.</p>
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,
 * which is appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Construct a producer</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the synchronous client
 * {@link com.azure.messaging.eventhubs.EventHubProducerClient}.  The {@code fullyQualifiedNamespace} is the Event Hubs
 * Namespace's host name.  It is listed under the "Essentials" panel after navigating to the Event Hubs Namespace via
 * Azure Portal.  The credential used is {@code DefaultAzureCredential} because it combines commonly used credentials
 * in deployment and development and chooses the credential to used based on its running environment.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubproducerclient.construct -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubProducerClient producer = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
 *         credential&#41;
 *     .buildProducerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubproducerclient.construct -->
 *
 * <p><strong>Sample: Construct a consumer</strong></p>
 *
 * @see com.azure.messaging.eventhubs.EventHubClientBuilder
 * @see com.azure.messaging.eventhubs.EventProcessorClientBuilder
 * @see <a href="https://learn.microsoft.com/azure/event-hubs/event-hubs-about">Azure Event Hubs</a>
 */
package com.azure.messaging.eventhubs;
