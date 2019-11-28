// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

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
 * </p>
 *
 * <p>
 * If no partition id is specified, the following rules are used for automatically selecting one:
 *
 * <ol>
 * <li>Distribute the events equally amongst all available partitions using a round-robin approach.</li>
 * <li>If a partition becomes unavailable, the Event Hubs service will automatically detect it and forward the
 * message to another available partition.</li>
 * </ol>
 * </p>
 *
 * <p><strong>Create a producer and publish events to any partition</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch}
 *
 * <p><strong>Publish events to partition "foo"</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionId}
 *
 * <p><strong>Publish events to the same partition, grouped together using partition key</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionKey}
 *
 * <p><strong>Publish events using a size-limited {@link EventDataBatch}</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-int}
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
    void send(EventData event, SendOptions options) {
        producer.send(event, options).block();
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param events Events to send to the service.
     */
    void send(Iterable<EventData> events) {
        producer.send(events).block();
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param events Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     */
    void send(Iterable<EventData> events, SendOptions options) {
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
