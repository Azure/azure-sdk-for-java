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
 * <h3>Publishing events</h3>
 *
 * <p>This library provides several ways to publish events to Azure Event Hubs.  There is a producer client, that sends
 * events immediately to Azure Event Hubs and a buffered producer, that batches events together in the background and
 * publishes them later. These two clients have synchronous and asynchronous versions.  The samples below demonstrate
 * simple scenarios, more snippets can be found in the class documentation for
 * {@link com.azure.messaging.eventhubs.EventHubProducerClient},
 * {@link com.azure.messaging.eventhubs.EventHubProducerAsyncClient},
 * {@link com.azure.messaging.eventhubs.EventHubBufferedProducerClient}, and
 * {@link com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient}.</p>
 *
 * <p>In the following snippets, {@code fullyQualifiedNamespace} is the Event Hubs Namespace's host name.  It is listed
 * under the "Essentials" panel after navigating to the Event Hubs Namespace via Azure Portal.  The credential used is
 * {@code DefaultAzureCredential} because it combines commonly used credentials in deployment and development and
 * chooses the credential to used based on its running environment.</p>
 *
 * <p><strong>Sample: Construct a synchronous producer and publish events</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the synchronous client
 * {@link com.azure.messaging.eventhubs.EventHubProducerClient}.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubproducerclient.createBatch -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * EventHubProducerClient producer = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
 *         credential&#41;
 *     .buildProducerClient&#40;&#41;;
 *
 * List&lt;EventData&gt; allEvents = Arrays.asList&#40;new EventData&#40;&quot;Foo&quot;&#41;, new EventData&#40;&quot;Bar&quot;&#41;&#41;;
 * EventDataBatch eventDataBatch = producer.createBatch&#40;&#41;;
 *
 * for &#40;EventData eventData : allEvents&#41; &#123;
 *     if &#40;!eventDataBatch.tryAdd&#40;eventData&#41;&#41; &#123;
 *         producer.send&#40;eventDataBatch&#41;;
 *         eventDataBatch = producer.createBatch&#40;&#41;;
 *
 *         &#47;&#47; Try to add that event that couldn't fit before.
 *         if &#40;!eventDataBatch.tryAdd&#40;eventData&#41;&#41; &#123;
 *             throw new IllegalArgumentException&#40;&quot;Event is too large for an empty batch. Max size: &quot;
 *                 + eventDataBatch.getMaxSizeInBytes&#40;&#41;&#41;;
 *         &#125;
 *     &#125;
 * &#125;
 *
 * &#47;&#47; send the last batch of remaining events
 * if &#40;eventDataBatch.getCount&#40;&#41; &gt; 0&#41; &#123;
 *     producer.send&#40;eventDataBatch&#41;;
 * &#125;
 *
 * &#47;&#47; Clients are expected to be long-lived objects.
 * &#47;&#47; Dispose of the producer to close any underlying resources when we are finished with it.
 * producer.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubproducerclient.createBatch -->
 *
 * <p><strong>Sample: Creating an {@link com.azure.messaging.eventhubs.EventHubBufferedProducerClient} and enqueuing
 * events</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the synchronous client
 * {@link com.azure.messaging.eventhubs.EventHubBufferedProducerClient} as well as enqueueing events.  The producer is
 * set to publish events every 60 seconds with a buffer size of 1500 events for each partition.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubbufferedproducerclient.enqueueEvents-iterable -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubBufferedProducerClient client = new EventHubBufferedProducerClientBuilder&#40;&#41;
 *     .credential&#40;&quot;fully-qualified-namespace&quot;, &quot;event-hub-name&quot;, credential&#41;
 *     .onSendBatchSucceeded&#40;succeededContext -&gt; &#123;
 *         System.out.println&#40;&quot;Successfully published events to: &quot; + succeededContext.getPartitionId&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .onSendBatchFailed&#40;failedContext -&gt; &#123;
 *         System.out.printf&#40;&quot;Failed to published events to %s. Error: %s%n&quot;,
 *             failedContext.getPartitionId&#40;&#41;, failedContext.getThrowable&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .buildClient&#40;&#41;;
 *
 * List&lt;EventData&gt; events = Arrays.asList&#40;new EventData&#40;&quot;maple&quot;&#41;, new EventData&#40;&quot;aspen&quot;&#41;,
 *     new EventData&#40;&quot;oak&quot;&#41;&#41;;
 *
 * &#47;&#47; Enqueues the events to be published.
 * client.enqueueEvents&#40;events&#41;;
 *
 * &#47;&#47; Seconds later, enqueue another event.
 * client.enqueueEvent&#40;new EventData&#40;&quot;bonsai&quot;&#41;&#41;;
 *
 * &#47;&#47; Causes any buffered events to be flushed before closing underlying connection.
 * client.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubbufferedproducerclient.enqueueEvents-iterable -->
 *
 * <h3>Consuming events</h3>
 *
 * <p>This library provides several ways to consume events from Azure Event Hubs.  There are consumer clients,
 * {@link com.azure.messaging.eventhubs.EventHubConsumerClient} and
 * {@link com.azure.messaging.eventhubs.EventHubConsumerAsyncClient}, which fetches events from either a single
 * partition or all partitions in an Event Hub.  For production, we recommend
 * {@link com.azure.messaging.eventhubs.EventProcessorClient} whose checkpoints are backed by a durable storage such as
 * Azure Blob Storage.  The samples below demonstrate simple scenarios, more snippets can be found in the class
 * documentation for {@link com.azure.messaging.eventhubs.EventHubConsumerClient},
 * {@link com.azure.messaging.eventhubs.EventHubConsumerAsyncClient}, and
 * {@link com.azure.messaging.eventhubs.EventProcessorClient}.</p>
 *
 * <p>In the following snippets, {@code fullyQualifiedNamespace} is the Event Hubs Namespace's host name.  It is listed
 * under the "Essentials" panel after navigating to the Event Hubs Namespace via Azure Portal.  The credential used is
 * {@code DefaultAzureCredential} because it combines commonly used credentials in deployment and development and
 * chooses the credential to used based on its running environment.  The {@code consumerGroup} is found by navigating
 * to the Event Hub instance, and selecting "Consumer groups" under the "Entities" panel. The {@code consumerGroup} is
 * required for creating consumer clients.</p>
 *
 * <p>The credential used is {@code DefaultAzureCredential} because it combines
 * commonly used credentials in deployment and development and chooses the credential to used based on its running
 * environment.</p>
 *
 * <p><strong>Sample: Construct a synchronous consumer and receive events</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the synchronous client
 * {@link com.azure.messaging.eventhubs.EventHubConsumerClient}.  In addition, it receives the first 100 events that
 * were enqueued 12 hours ago.  If there are less than 100 events, the ones fetched within {@code maxWaitTime} of 30
 * seconds are returned.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubconsumerclient.receive#string-int-eventposition-duration -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubConsumerClient consumer = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
 *         credential&#41;
 *     .consumerGroup&#40;EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME&#41;
 *     .buildConsumerClient&#40;&#41;;
 *
 * Instant twelveHoursAgo = Instant.now&#40;&#41;.minus&#40;Duration.ofHours&#40;12&#41;&#41;;
 * EventPosition startingPosition = EventPosition.fromEnqueuedTime&#40;twelveHoursAgo&#41;;
 * String partitionId = &quot;0&quot;;
 *
 * &#47;&#47; Reads events from partition '0' and returns the first 100 received or until the 30 seconds has elapsed.
 * IterableStream&lt;PartitionEvent&gt; events = consumer.receiveFromPartition&#40;partitionId, 100,
 *     startingPosition, Duration.ofSeconds&#40;30&#41;&#41;;
 *
 * Long lastSequenceNumber = -1L;
 * for &#40;PartitionEvent partitionEvent : events&#41; &#123;
 *     &#47;&#47; For each event, perform some sort of processing.
 *     System.out.print&#40;&quot;Event received: &quot; + partitionEvent.getData&#40;&#41;.getSequenceNumber&#40;&#41;&#41;;
 *     lastSequenceNumber = partitionEvent.getData&#40;&#41;.getSequenceNumber&#40;&#41;;
 * &#125;
 *
 * &#47;&#47; Figure out what the next EventPosition to receive from is based on last event we processed in the stream.
 * &#47;&#47; If lastSequenceNumber is -1L, then we didn't see any events the first time we fetched events from the
 * &#47;&#47; partition.
 * if &#40;lastSequenceNumber != -1L&#41; &#123;
 *     EventPosition nextPosition = EventPosition.fromSequenceNumber&#40;lastSequenceNumber, false&#41;;
 *
 *     &#47;&#47; Gets the next set of events from partition '0' to consume and process.
 *     IterableStream&lt;PartitionEvent&gt; nextEvents = consumer.receiveFromPartition&#40;partitionId, 100,
 *         nextPosition, Duration.ofSeconds&#40;30&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubconsumerclient.receive#string-int-eventposition-duration -->
 *
 * <p><strong>Sample: Construct an {@link com.azure.messaging.eventhubs.EventProcessorClient}</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the processor client.  The processor client is recommended
 * for production scenarios because it can load balance between multiple running instances, can perform checkpointing,
 * and reconnects on transient failures such as network outages.  The sample below uses an in-memory
 * {@link com.azure.messaging.eventhubs.CheckpointStore} but
 * <a href="https://central.sonatype.com/artifact/com.azure/azure-messaging-eventhubs-checkpointstore-blob">
 *     azure-messaging-eventhubs-checkpointstore-blob</a> provides a checkpoint store backed by Azure Blob Storage.
 * </p>
 *
 *<!-- src_embed com.azure.messaging.eventhubs.eventprocessorclient.startstop -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder&#40;&#41;
 *     .consumerGroup&#40;EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME&#41;
 *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
 *         credential&#41;
 *     .processEvent&#40;eventContext -&gt; &#123;
 *         System.out.printf&#40;&quot;Partition id = %s and sequence number of event = %s%n&quot;,
 *             eventContext.getPartitionContext&#40;&#41;.getPartitionId&#40;&#41;,
 *             eventContext.getEventData&#40;&#41;.getSequenceNumber&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .processError&#40;errorContext -&gt; &#123;
 *         System.out.printf&#40;&quot;Error occurred in partition processor for partition %s, %s%n&quot;,
 *             errorContext.getPartitionContext&#40;&#41;.getPartitionId&#40;&#41;,
 *             errorContext.getThrowable&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .checkpointStore&#40;new SampleCheckpointStore&#40;&#41;&#41;
 *     .buildEventProcessorClient&#40;&#41;;
 *
 * eventProcessorClient.start&#40;&#41;;
 *
 * &#47;&#47; Continue to perform other tasks while the processor is running in the background.
 * &#47;&#47;
 * &#47;&#47; Finally, stop the processor client when application is finished.
 * eventProcessorClient.stop&#40;&#41;;
 * </pre>
 *<!-- end com.azure.messaging.eventhubs.eventprocessorclient.startstop -->
 *
 * @see com.azure.messaging.eventhubs.EventHubClientBuilder
 * @see com.azure.messaging.eventhubs.EventProcessorClientBuilder
 * @see <a href="https://learn.microsoft.com/azure/event-hubs/event-hubs-about">Azure Event Hubs</a>
 */
package com.azure.messaging.eventhubs;
