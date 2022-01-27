// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.models.SendOptions;

import java.io.Closeable;
import java.time.Duration;
import java.util.Objects;

/**
 * An <strong>synchronous</strong> producer responsible for transmitting {@link EventData} to a specific Event Hub
 * without building and managing batches.
 */
@ServiceClient(builder = EventHubBufferedProducerClientBuilder.class)
public final class EventHubBufferedProducerClient implements Closeable {
    private final EventHubBufferedProducerAsyncClient producer;
    private final Duration tryTimeout;
    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();

    EventHubBufferedProducerClient(EventHubBufferedProducerAsyncClient producer, Duration tryTimeout) {
        this.producer = Objects.requireNonNull(producer, "'producer' cannot be null.");
        this.tryTimeout = tryTimeout;
    }

    /**
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with
     */
    public String getFullyQualifiedNamespace() {
        return producer.getFullyQualifiedNamespace();
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
        return  new IterableStream<>(producer.getPartitionIds());
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PartitionProperties getPartitionProperties(String partitionId) {
        return producer.getPartitionProperties(partitionId).block(tryTimeout);
    }

    /**
     * Retrieves the quantity of events in the buffered client that have not been sent.
     *
     * @return The quantity of events.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public int getBufferedEventCount() {
        return producer.getBufferedEventCount();
    }

    /**
     * Retrieves the quantity of events in the buffered client that will send to a specific partition.
     *
     * @param partitionId The unique identifier of a partition associated with the Event Hub.
     *
     * @return the quantity of events for the specific partition.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public int getBufferedEventCount(String partitionId) {
        return producer.getBufferedEventCount(partitionId);
    }

    /**
     * Enqueue a single event without send options, will be sent to event hub by default send options.
     *
     * @param eventData The {@link EventData} to store in client temporarily to send to event hub.
     * @throws AmqpException if the size of {@code events} exceed the maximum size of a single batch.
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void enqueueEvent(EventData eventData) {
        return producer.enqueueEvent(eventData, DEFAULT_SEND_OPTIONS).block(tryTimeout);
    }

    /**
     * Enqueue a single event with send options.
     *
     * @param eventData The {@link EventData} will be stored in client temporarily and sent to event hub.
     * @param options A set of options used to configure the {@link EventDataBatch}.
     * @throws AmqpException if the size of {@code events} exceed the maximum size of a single batch.
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void enqueueEvent(EventData eventData, SendOptions options) {
        return producer.enqueueEvent(eventData, options).block(tryTimeout);
    }

    /**
     * Enqueue iterable events without send options, will be sent to event hub by default send options.
     *
     * @param events Events will be stored in client temporarily and sent to event hub.
     * @throws AmqpException if the size of {@code events} exceed the maximum size of a single batch.
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void enqueueEvents(Iterable<EventData> events) {
        return producer.enqueueEvents(events, DEFAULT_SEND_OPTIONS).block(tryTimeout);
    }

    /**
     * Enqueue iterable events with send options.
     *
     * @param events Events will be stored in client temporarily and sent to event hub.
     * @param options A set of options used to configure the {@link EventDataBatch}.
     * @throws AmqpException if the size of {@code events} exceed the maximum size of a single batch.
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void enqueueEvents(Iterable<EventData> events, SendOptions options) {
        return producer.enqueueEvents(events, options).block(tryTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        producer.close();
    }

}
