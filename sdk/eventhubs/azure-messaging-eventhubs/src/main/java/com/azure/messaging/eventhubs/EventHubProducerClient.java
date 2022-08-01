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
import java.time.Duration;
import java.util.Objects;

/**
 * A <b>synchronous</b> producer responsible for transmitting {@link EventData} to a specific Event Hub, grouped
 * together in batches. Depending on the {@link CreateBatchOptions options} specified when creating an
 * {@link EventDataBatch}, the events may be automatically routed to an available partition or specific to a partition.
 *
 * <p>
 * Allowing automatic routing of partitions is recommended when:
 * <ul>
 * <li>The sending of events needs to be highly available.</li>
 * <li>The event data should be evenly distributed among all available partitions.</li>
 * </ul>
 *
 * <p>
 * If no partition id is specified, the following rules are used for automatically selecting one:
 *
 * <ol>
 * <li>Distribute the events equally amongst all available partitions using a round-robin approach.</li>
 * <li>If a partition becomes unavailable, the Event Hubs service will automatically detect it and forward the
 * message to another available partition.</li>
 * </ol>
 *
 * <p><strong>Create a producer and publish events to any partition</strong></p>
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubproducerclient.createBatch -->
 * <pre>
 * &#47;&#47; The required parameter is a way to authenticate with Event Hubs using credentials.
 * &#47;&#47; The connectionString provides a way to authenticate with Event Hub.
 * EventHubProducerClient producer = new EventHubClientBuilder&#40;&#41;
 *     .connectionString&#40;
 *         &quot;Endpoint=&#123;fully-qualified-namespace&#125;;SharedAccessKeyName=&#123;policy-name&#125;;SharedAccessKey=&#123;key&#125;&quot;,
 *         &quot;event-hub-name&quot;&#41;
 *     .buildProducerClient&#40;&#41;;
 * List&lt;EventData&gt; events = Arrays.asList&#40;new EventData&#40;&quot;test-event-1&quot;&#41;, new EventData&#40;&quot;test-event-2&quot;&#41;&#41;;
 *
 * &#47;&#47; Creating a batch without options set, will allow for automatic routing of events to any partition.
 * EventDataBatch batch = producer.createBatch&#40;&#41;;
 * for &#40;EventData event : events&#41; &#123;
 *     if &#40;batch.tryAdd&#40;event&#41;&#41; &#123;
 *         continue;
 *     &#125;
 *
 *     producer.send&#40;batch&#41;;
 *     batch = producer.createBatch&#40;&#41;;
 *     if &#40;!batch.tryAdd&#40;event&#41;&#41; &#123;
 *         throw new IllegalArgumentException&#40;&quot;Event is too large for an empty batch.&quot;&#41;;
 *     &#125;
 * &#125;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubproducerclient.createBatch -->
 *
 * <p><strong>Publish events to partition "foo"</strong></p>
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionId -->
 * <pre>
 * &#47;&#47; Creating a batch with partitionId set will route all events in that batch to partition `foo`.
 * CreateBatchOptions options = new CreateBatchOptions&#40;&#41;.setPartitionId&#40;&quot;foo&quot;&#41;;
 *
 * EventDataBatch batch = producer.createBatch&#40;options&#41;;
 * batch.tryAdd&#40;new EventData&#40;&quot;data-to-partition-foo&quot;&#41;&#41;;
 * producer.send&#40;batch&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionId -->
 *
 * <p><strong>Publish events to the same partition, grouped together using partition key</strong></p>
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
 * <p><strong>Publish events using a size-limited {@link EventDataBatch}</strong></p>
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
 * @see EventHubClientBuilder#buildProducerClient()
 * @see EventHubProducerAsyncClient To asynchronously generate events to an Event Hub, see EventHubProducerAsyncClient.
 */
@ServiceClient(builder = EventHubClientBuilder.class)
public class EventHubProducerClient implements Closeable {
    private final EventHubProducerAsyncClient producer;
    private final Duration tryTimeout;

    /**
     * Creates a new instance of {@link EventHubProducerClient} that sends messages to an Azure Event Hub.
     *
     * @throws NullPointerException if {@code producer} or {@code tryTimeout} is null.
     */
    EventHubProducerClient(EventHubProducerAsyncClient producer, Duration tryTimeout) {
        this.producer = Objects.requireNonNull(producer, "'producer' cannot be null.");
        this.tryTimeout = Objects.requireNonNull(tryTimeout, "'tryTimeout' cannot be null.");
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
        return producer.getEventHubProperties().block(tryTimeout);
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
        return producer.getPartitionProperties(partitionId).block(tryTimeout);
    }

    /**
     * Creates an {@link EventDataBatch} that can fit as many events as the transport allows.
     *
     * @return A new {@link EventDataBatch} that can fit as many events as the transport allows.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EventDataBatch createBatch() {
        return producer.createBatch().block(tryTimeout);
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
        return producer.createBatch(options).block(tryTimeout);
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
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
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
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * <!-- src_embed com.azure.messaging.eventhubs.eventhubproducerclient.send#Iterable-SendOptions -->
     * <pre>
     * List&lt;EventData&gt; events = Arrays.asList&#40;new EventData&#40;&quot;Melbourne&quot;&#41;, new EventData&#40;&quot;London&quot;&#41;,
     *     new EventData&#40;&quot;New York&quot;&#41;&#41;;
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
}
