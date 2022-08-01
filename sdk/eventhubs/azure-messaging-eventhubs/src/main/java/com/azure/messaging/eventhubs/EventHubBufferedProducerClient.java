// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;


import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.models.SendOptions;

import java.io.Closeable;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * A client responsible for publishing instances of {@link EventData} to a specific Event Hub.  Depending on the options
 * specified when events are enqueued, they may be automatically assigned to a partition, grouped according to the
 * specified partition key, or assigned a specifically requested partition.
 *
 * <p>
 * The {@link EventHubBufferedProducerClient} does not publish immediately, instead using a deferred model where events
 * are collected into a buffer so that they may be efficiently batched and published when the batch is full or the
 * {@link EventHubBufferedProducerClientBuilder#maxWaitTime(Duration) maxWaitTime} has elapsed with no new events
 * enqueued.
 * </p>
 * <p>
 * This model is intended to shift the burden of batch management from callers, at the cost of non-deterministic timing,
 * for when events will be published. There are additional trade-offs to consider, as well:
 * </p>
 * <ul>
 * <li>If the application crashes, events in the buffer will not have been published.  To
 * prevent data loss, callers are encouraged to track publishing progress using
 * {@link EventHubBufferedProducerClientBuilder#onSendBatchFailed(Consumer) onSendBatchFailed} and
 * {@link EventHubBufferedProducerClientBuilder#onSendBatchSucceeded(Consumer) onSendBatchSucceeded}.</li>
 * <li>Events specifying a partition key may be assigned a different partition than those
 * using the same key with other producers.</li>
 * <li>In the unlikely event that a partition becomes temporarily unavailable,
 * the {@link EventHubBufferedProducerClient} may take longer to recover than other producers.</li>
 * </ul>
 * <p>
 * In scenarios where it is important to have events published immediately with a deterministic outcome, ensure that
 * partition keys are assigned to a partition consistent with other publishers, or where maximizing availability is a
 * requirement, using {@link EventHubProducerAsyncClient} or {@link EventHubProducerClient} is recommended.
 * </p>
 */
@ServiceClient(builder = EventHubBufferedProducerClientBuilder.class, isAsync = false)
public final class EventHubBufferedProducerClient implements Closeable {
    private final EventHubBufferedProducerAsyncClient client;
    private final Duration operationTimeout;

    EventHubBufferedProducerClient(EventHubBufferedProducerAsyncClient asyncClient, Duration operationTimeout) {
        this.client = asyncClient;
        this.operationTimeout = operationTimeout;
    }

    /**
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with
     */
    public String getFullyQualifiedNamespace() {
        return client.getFullyQualifiedNamespace();
    }

    /**
     * Gets the Event Hub name this client interacts with.
     *
     * @return The Event Hub name this client interacts with.
     */
    public String getEventHubName() {
        return client.getEventHubName();
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EventHubProperties getEventHubProperties() {
        return client.getEventHubProperties().block(operationTimeout);
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return A stream of identifiers for the partitions of an Event Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public IterableStream<String> getPartitionIds() {
        return new IterableStream<>(client.getPartitionIds());
    }

    /**
     * Retrieves information about a specific partition for an Event Hub, including elements that describe the available
     * events in the partition event stream.
     *
     * @param partitionId The unique identifier of a partition associated with the Event Hub.
     *
     * @return The set of information for the requested partition under the Event Hub this client is associated with.
     *
     * @throws NullPointerException if {@code partitionId} is null.
     * @throws IllegalArgumentException if {@code partitionId} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PartitionProperties getPartitionProperties(String partitionId) {
        return client.getPartitionProperties(partitionId).block(operationTimeout);
    }

    /**
     * Gets the total number of events that are currently buffered and waiting to be published, across all partitions.
     *
     * @return The total number of events that are currently buffered and waiting to be published, across all
     *     partitions.
     */
    public int getBufferedEventCount() {
        return client.getBufferedEventCount();
    }

    /**
     * Gets the number of events that are buffered and waiting to be published for a given partition.
     *
     * @param partitionId The partition identifier.
     *
     * @return The number of events that are buffered and waiting to be published for a given partition.
     *
     * @throws NullPointerException if {@code partitionId} is null.
     * @throws IllegalArgumentException if {@code partitionId} is empty.
     */
    public int getBufferedEventCount(String partitionId) {
        return client.getBufferedEventCount(partitionId);
    }

    /**
     * Enqueues an {@link EventData} into the buffer to be published to the Event Hub.  If there is no capacity in the
     * buffer when this method is invoked, it will wait for space to become available and ensure that the {@code
     * eventData} has been enqueued.
     *
     * When this call returns, the {@code eventData} has been accepted into the buffer, but it may not have been
     * published yet. Publishing will take place at a nondeterministic point in the future as the buffer is processed.
     *
     * @param eventData The event to be enqueued into the buffer and, later, published.
     *
     * @return The total number of events that are currently buffered and waiting to be published, across all
     *     partitions.
     *
     * @throws NullPointerException if {@code eventData} is null.
     * @throws IllegalStateException if the producer was closed while queueing an event.
     */
    public Integer enqueueEvent(EventData eventData) {
        return client.enqueueEvent(eventData).block(operationTimeout);
    }

    /**
     * Enqueues an {@link EventData} into the buffer to be published to the Event Hub.  If there is no capacity in the
     * buffer when this method is invoked, it will wait for space to become available and ensure that the {@code
     * eventData} has been enqueued.
     *
     * When this call returns, the {@code eventData} has been accepted into the buffer, but it may not have been
     * published yet. Publishing will take place at a nondeterministic point in the future as the buffer is processed.
     *
     * @param eventData The event to be enqueued into the buffer and, later, published.
     * @param options The set of options to apply when publishing this event.  If partitionKey and partitionId are
     *     not set, then the event is distributed round-robin amongst all the partitions.
     *
     * @return The total number of events that are currently buffered and waiting to be published, across all
     *     partitions.
     *
     * @throws NullPointerException if {@code eventData} or {@code options} is null.
     * @throws IllegalArgumentException if {@link SendOptions#getPartitionId() getPartitionId} is set and is not
     *     valid.
     * @throws IllegalStateException if the producer was closed while queueing an event.
     */
    public Integer enqueueEvent(EventData eventData, SendOptions options) {
        return client.enqueueEvent(eventData, options).block(operationTimeout);
    }

    /**
     * Enqueues a set of {@link EventData} into the buffer to be published to the Event Hub.  If there is insufficient
     * capacity in the buffer when this method is invoked, it will wait for space to become available and ensure that
     * all EventData in the {@code events} set have been enqueued.
     *
     * When this call returns, the {@code events} have been accepted into the buffer, but it may not have been published
     * yet. Publishing will take place at a nondeterministic point in the future as the buffer is processed.
     *
     * @param events The set of events to be enqueued into the buffer and, later, published.
     *
     * @return The total number of events that are currently buffered and waiting to be published, across all
     *     partitions.
     *
     * @throws NullPointerException if {@code events} is null.
     * @throws IllegalStateException if the producer was closed while queueing an event.
     */
    public Integer enqueueEvents(Iterable<EventData> events) {
        return client.enqueueEvents(events).block(operationTimeout);
    }

    /**
     * Enqueues a set of {@link EventData} into the buffer to be published to the Event Hub.  If there is insufficient
     * capacity in the buffer when this method is invoked, it will wait for space to become available and ensure that
     * all EventData in the {@code events} set have been enqueued.
     *
     * When this call returns, the {@code events} have been accepted into the buffer, but it may not have been published
     * yet. Publishing will take place at a nondeterministic point in the future as the buffer is processed.
     *
     * @param events The set of events to be enqueued into the buffer and, later, published.
     * @param options The set of options to apply when publishing this event.
     *
     * @return The total number of events that are currently buffered and waiting to be published, across all
     *     partitions.
     *
     * @throws NullPointerException if {@code eventData} or {@code options} is null.
     * @throws IllegalArgumentException if {@link SendOptions#getPartitionId() getPartitionId} is set and is not
     *     valid.
     * @throws IllegalStateException if the producer was closed while queueing an event.
     */
    public Integer enqueueEvents(Iterable<EventData> events, SendOptions options) {
        return client.enqueueEvents(events, options).block(operationTimeout);
    }

    /**
     * Attempts to publish all events in the buffer immediately.  This may result in multiple batches being published,
     * the outcome of each of which will be individually reported by the {@link EventHubBufferedProducerClientBuilder#onSendBatchFailed(Consumer)}
     * and {@link EventHubBufferedProducerClientBuilder#onSendBatchSucceeded(Consumer)} handlers.
     *
     * Upon completion of this method, the buffer will be empty.
     */
    public void flush() {
        client.flush().block(operationTimeout.plus(operationTimeout));
    }

    /**
     * Disposes of the producer and all its resources.
     */
    @Override
    public void close() {
        client.close();
    }
}
