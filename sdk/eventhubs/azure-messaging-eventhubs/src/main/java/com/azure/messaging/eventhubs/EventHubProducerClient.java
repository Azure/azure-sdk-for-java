// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;

import java.io.Closeable;
import java.util.Objects;

/**
 * <p>A <b>synchronous</b> producer responsible for transmitting {@link EventData} to a specific Event Hub, grouped
 * together in batches. Depending on the {@link CreateBatchOptions options} specified when creating an
 * {@link EventDataBatch}, the events may be automatically routed to an available partition or specific to a partition.
 * More information and specific recommendations for strategies to use when publishing events is in:
 * <a href="https://learn.microsoft.com/azure/architecture/reference-architectures/event-hubs/partitioning-in-event-hubs-and-kafka#distribute-events-to-partitions">
 *     Distribute events to partitions</a></p>
 *
 * <p>Allowing automatic routing of partitions is recommended when:</p>
 * <ul>
 *   <li>The sending of events needs to be highly available.</li>
 *   <li>The event data should be evenly distributed among all available partitions.</li>
 * </ul>
 *
 * <p>If no partition id is specified, the following rules are used for automatically selecting one:</p>
 * <ol>
 *      <li>Distribute the events equally amongst all available partitions using a round-robin approach.</li>
 *      <li>If a partition becomes unavailable, the Event Hubs service will automatically detect it and forward the
 *      message to another available partition.</li>
 * </ol>
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
 * <p><strong>Sample: Construct a {@link EventHubProducerClient}</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the synchronous client {@link EventHubProducerClient}.
 * The {@code fullyQualifiedNamespace} is the Event Hubs Namespace's host name.  It is listed under the "Essentials"
 * panel after navigating to the Event Hubs Namespace via Azure Portal. </p>
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
 * <p><strong>Sample: Create a producer and publish events to any partition</strong></p>
 *
 * <p>The following code sample demonstrates publishing events and allowing the service to distribute the events
 * round-robin between all partitions.</p>
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
 * <p><strong>Sample: Publish events to partition "0"</strong></p>
 *
 *  <p>The following code sample demonstrates publishing events to a specific partition.  In the scenario below, all
 * events are sent to partition "0".</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionId -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * EventHubProducerClient producer = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
 *         credential&#41;
 *     .buildProducerClient&#40;&#41;;
 *
 * &#47;&#47; Creating a batch with partitionId set will route all events in that batch to partition `0`.
 * CreateBatchOptions options = new CreateBatchOptions&#40;&#41;.setPartitionId&#40;&quot;0&quot;&#41;;
 * EventDataBatch batch = producer.createBatch&#40;options&#41;;
 *
 * &#47;&#47; Add events to batch and when you want to send the batch, send it using the producer.
 * producer.send&#40;batch&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionId -->
 *
 * <p><strong>Sample: Publish events to the same partition, grouped together using partition key</strong></p>
 *
 * <p>The sample code below uses {@link CreateBatchOptions#setPartitionKey(String)} when creating the
 * {@link EventDataBatch}.  All events added to this batch will be published to the same partition.  In general, events
 * with the same {@code partitionKey} end up in the same partition.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionKey -->
 * <pre>
 * List&lt;EventData&gt; events = Arrays.asList&#40;new EventData&#40;&quot;sourdough&quot;&#41;, new EventData&#40;&quot;rye&quot;&#41;,
 *     new EventData&#40;&quot;wheat&quot;&#41;&#41;;
 *
 * &#47;&#47; Creating a batch with partitionKey set will tell the service to hash the partitionKey and decide which
 * &#47;&#47; partition to send the events to. Events with the same partitionKey are always routed to the same partition.
 * CreateBatchOptions options = new CreateBatchOptions&#40;&#41;.setPartitionKey&#40;&quot;bread&quot;&#41;;
 * EventDataBatch batch = producer.createBatch&#40;options&#41;;
 *
 * events.forEach&#40;event -&gt; batch.tryAdd&#40;event&#41;&#41;;
 * producer.send&#40;batch&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionKey -->
 *
 * <p><strong>Sample: Publish events using a size-limited {@link EventDataBatch}</strong></p>
 *
 * <p>The sample code below uses {@link CreateBatchOptions#setMaximumSizeInBytes(int)} when creating the
 * {@link EventDataBatch}.  In the example, it limits the size of the batch to 256 bytes.  This is useful for scenarios
 * where there are constraints like network throughput, memory, etc.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-int -->
 * <pre>
 * List&lt;EventData&gt; telemetryEvents = Arrays.asList&#40;firstEvent, secondEvent, thirdEvent&#41;;
 *
 * &#47;&#47; Setting `setMaximumSizeInBytes` when creating a batch, limits the size of that batch.
 * &#47;&#47; In this case, all the batches created with these options are limited to 256 bytes.
 * CreateBatchOptions options = new CreateBatchOptions&#40;&#41;.setMaximumSizeInBytes&#40;256&#41;;
 *
 * EventDataBatch currentBatch = producer.createBatch&#40;options&#41;;
 *
 * &#47;&#47; For each telemetry event, we try to add it to the current batch.
 * &#47;&#47; When the batch is full, send it then create another batch to add more events to.
 * for &#40;EventData event : telemetryEvents&#41; &#123;
 *     if &#40;!currentBatch.tryAdd&#40;event&#41;&#41; &#123;
 *         producer.send&#40;currentBatch&#41;;
 *         currentBatch = producer.createBatch&#40;options&#41;;
 *
 *         &#47;&#47; Add the event we couldn't before.
 *         if &#40;!currentBatch.tryAdd&#40;event&#41;&#41; &#123;
 *             throw new IllegalArgumentException&#40;&quot;Event is too large for an empty batch.&quot;&#41;;
 *         &#125;
 *     &#125;
 * &#125;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-int -->
 *
 * @see com.azure.messaging.eventhubs
 * @see EventHubClientBuilder
 * @see EventHubProducerAsyncClient To asynchronously generate events to an Event Hub, see EventHubProducerAsyncClient.
 */
@ServiceClient(builder = EventHubClientBuilder.class)
public class EventHubProducerClient implements Closeable {
    private final EventHubProducerAsyncClient producer;

    /**
     * Creates a new instance of {@link EventHubProducerClient} that sends messages to an Azure Event Hub.
     *
     * @throws NullPointerException if {@code producer} or {@code tryTimeout} is null.
     */
    EventHubProducerClient(EventHubProducerAsyncClient producer) {
        this.producer = Objects.requireNonNull(producer, "'producer' cannot be null.");
    }

    /**
     * Gets the Event Hub name this client interacts with.
     *
     * @return The Event Hub name this client interacts with.
     */
    public String getEventHubName() {
        return producer.getEventHubName();
    }

    /**
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with.
     */
    public String getFullyQualifiedNamespace() {
        return producer.getFullyQualifiedNamespace();
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EventHubProperties getEventHubProperties() {
        return producer.getEventHubProperties().block();
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return A Flux of identifiers for the partitions of an Event Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public IterableStream<String> getPartitionIds() {
        return new IterableStream<>(producer.getPartitionIds());
    }

    /**
     * Retrieves information about a specific partition for an Event Hub, including elements that describe the available
     * events in the partition event stream.
     *
     * @param partitionId The unique identifier of a partition associated with the Event Hub.
     * @return The set of information for the requested partition under the Event Hub this client is associated with.
     * @throws NullPointerException if {@code partitionId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PartitionProperties getPartitionProperties(String partitionId) {
        return producer.getPartitionProperties(partitionId).block();
    }

    /**
     * Creates an {@link EventDataBatch} that can fit as many events as the transport allows.
     *
     * @return A new {@link EventDataBatch} that can fit as many events as the transport allows.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EventDataBatch createBatch() {
        return producer.createBatch().block();
    }

    /**
     * Creates an {@link EventDataBatch} configured with the options specified.
     *
     * @param options A set of options used to configure the {@link EventDataBatch}.
     *
     * @return A new {@link EventDataBatch} that can fit as many events as the transport allows.
     *
     * @throws NullPointerException if {@code options} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EventDataBatch createBatch(CreateBatchOptions options) {
        return producer.createBatch(options).block();
    }

    /**
     * Sends a single event to the associated Event Hub. If the size of the single event exceeds the maximum size
     * allowed, an exception will be triggered and the send will fail.
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param event Event to send to the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    void send(EventData event) {
        producer.send(event).block();
    }

    /**
     * Sends a single event to the associated Event Hub with the send options. If the size of the single event exceeds
     * the maximum size allowed, an exception will be triggered and the send will fail.
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param event Event to send to the service.
     * @param options The set of options to consider when sending this event.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    void send(EventData event, SendOptions options) {
        producer.send(event, options).block();
    }

    /**
     * <p>Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.</p>
     *
     * <!-- src_embed com.azure.messaging.eventhubs.eventhubproducerclient.send#Iterable -->
     * <pre>
     * List&lt;EventData&gt; events = Arrays.asList&#40;new EventData&#40;&quot;maple&quot;&#41;, new EventData&#40;&quot;aspen&quot;&#41;,
     *     new EventData&#40;&quot;oak&quot;&#41;&#41;;
     * producer.send&#40;events&#41;;
     * </pre>
     * <!-- end com.azure.messaging.eventhubs.eventhubproducerclient.send#Iterable -->
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param events Events to send to the service.
     * @throws AmqpException if the size of {@code events} exceed the maximum size of a single batch.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void send(Iterable<EventData> events) {
        producer.send(events).block();
    }

    /**
     * <p>Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.</p>
     *
     * <!-- src_embed com.azure.messaging.eventhubs.eventhubproducerclient.send#Iterable-SendOptions -->
     * <pre>
     * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
     *
     * EventHubProducerClient producer = new EventHubClientBuilder&#40;&#41;
     *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
     *         credential&#41;
     *     .buildProducerClient&#40;&#41;;
     *
     * List&lt;EventData&gt; events = Arrays.asList&#40;new EventData&#40;&quot;Melbourne&quot;&#41;, new EventData&#40;&quot;London&quot;&#41;,
     *     new EventData&#40;&quot;New York&quot;&#41;&#41;;
     *
     * SendOptions sendOptions = new SendOptions&#40;&#41;.setPartitionKey&#40;&quot;cities&quot;&#41;;
     * producer.send&#40;events, sendOptions&#41;;
     * </pre>
     * <!-- end com.azure.messaging.eventhubs.eventhubproducerclient.send#Iterable-SendOptions -->
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param events Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     * @throws AmqpException if the size of {@code events} exceed the maximum size of a single batch.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void send(Iterable<EventData> events, SendOptions options) {
        producer.send(events, options).block();
    }

    /**
     * Sends the batch to the associated Event Hub.
     *
     * @param batch The batch to send to the service.
     * @throws NullPointerException if {@code batch} is {@code null}.
     * @see EventHubProducerClient#createBatch()
     * @see EventHubProducerClient#createBatch(CreateBatchOptions)
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void send(EventDataBatch batch) {
        producer.send(batch).block();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        producer.close();
    }

    /**
     * Gets the client identifier.
     *
     * @return The unique identifier string for current client.
     */
    public String getIdentifier() {
        return producer.getIdentifier();
    }

}
