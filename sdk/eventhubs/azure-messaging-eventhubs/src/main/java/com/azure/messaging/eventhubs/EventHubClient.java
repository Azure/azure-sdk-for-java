// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;

import java.io.Closeable;
import java.util.Objects;

/**
 * A <strong>synchronous</strong> client that is the main point of interaction with Azure Event Hubs. It connects to a
 * specific Event Hub and allows operations for sending event data, receiving data, and inspecting the Event Hub's
 * metadata.
 *
 * @see EventHubClientBuilder
 * @see EventHubAsyncClient To communicate with Event Hub using an asynchronous client.
 * @see <a href="https://docs.microsoft.com/Azure/event-hubs/event-hubs-about">About Azure Event Hubs</a>
 */
class EventHubClient implements Closeable {
    private final EventHubAsyncClient client;
    private final RetryOptions retry;

    EventHubClient(EventHubAsyncClient client, ConnectionOptions connectionOptions) {
        Objects.requireNonNull(connectionOptions, "'connectionOptions' cannot be null.");

        this.client = Objects.requireNonNull(client, "'client' cannot be null.");
        this.retry = connectionOptions.getRetry();
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
    public EventHubProperties getProperties() {
        return client.getProperties().block(retry.getTryTimeout());
    }

    /**
     * Retrieves the identifiers for all the partitions of an Event Hub.
     *
     * @return The identifiers for all partitions of an Event Hub.
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
     * @return The information for the requested partition under the Event Hub this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PartitionProperties getPartitionProperties(String partitionId) {
        return client.getPartitionProperties(partitionId).block(retry.getTryTimeout());
    }

    /**
     * Creates an Event Hub producer responsible for transmitting {@link EventData} to the Event Hub, grouped together
     * in batches. Event data is automatically routed to an available partition.
     *
     * @return A new {@link EventHubProducerClient}.
     */
    public EventHubProducerClient createProducer() {
        final EventHubProducerAsyncClient producer = client.createProducer();
        return new EventHubProducerClient(producer, retry.getTryTimeout());
    }

    /**
     * Creates an Event Hub consumer responsible for reading {@link EventData} from a specific Event Hub partition, as a
     * member of the specified consumer group, and begins reading events from the {@code eventPosition}.
     *
     * The consumer created is non-exclusive, allowing multiple consumers from the same consumer group to be actively
     * reading events from the partition. These non-exclusive consumers are sometimes referred to as "Non-epoch
     * Consumers".
     *
     * @param consumerGroup The name of the consumer group this consumer is associated with. Events are read in the
     *     context of this group. The name of the consumer group that is created by default is {@link
     *     EventHubClientBuilder#DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @return A new {@link EventHubConsumerClient} that receives events from the partition at the given position.
     * @throws NullPointerException If {@code eventPosition}, {@code consumerGroup}, {@code partitionId}, or
     *     {@code options} is {@code null}.
     * @throws IllegalArgumentException If {@code consumerGroup} or {@code partitionId} is an empty string.
     */
    public EventHubConsumerClient createConsumer(String consumerGroup, EventPosition eventPosition) {
        final EventHubConsumerAsyncClient consumer = client.createConsumer(consumerGroup, eventPosition);
        return new EventHubConsumerClient(consumer, retry.getTryTimeout());
    }

    /**
     * Creates an Event Hub consumer responsible for reading {@link EventData} from a specific Event Hub partition, as a
     * member of the configured consumer group, and begins reading events from the specified {@code eventPosition}.
     *
     * <p>
     * A consumer may be exclusive, which asserts ownership over the partition for the consumer group to ensure that
     * only one consumer from that group is reading from the partition. These exclusive consumers are sometimes referred
     * to as "Epoch Consumers."
     *
     * A consumer may also be non-exclusive, allowing multiple consumers from the same consumer group to be actively
     * reading events from the partition. These non-exclusive consumers are sometimes referred to as "Non-epoch
     * Consumers."
     *
     * Designating a consumer as exclusive may be specified in the {@code options}, by setting {@link
     * EventHubConsumerOptions#setOwnerLevel(Long)} to a non-null value. By default, consumers are created as
     * non-exclusive.
     * </p>
     *
     * @param consumerGroup The name of the consumer group this consumer is associated with. Events are read in the
     *     context of this group. The name of the consumer group that is created by default is {@link
     *     EventHubClientBuilder#DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @param options The set of options to apply when creating the consumer.
     * @return An new {@link EventHubConsumerClient} that receives events from the partition with all configured {@link
     *     EventHubConsumerOptions}.
     * @throws NullPointerException If {@code eventPosition}, {@code consumerGroup}, {@code partitionId}, or
     *     {@code options} is {@code null}.
     * @throws IllegalArgumentException If {@code consumerGroup} or {@code partitionId} is an empty string.
     */
    public EventHubConsumerClient createConsumer(String consumerGroup, EventPosition eventPosition,
            EventHubConsumerOptions options) {
        final EventHubConsumerAsyncClient consumer = client.createConsumer(consumerGroup, eventPosition, options);

        return new EventHubConsumerClient(consumer, retry.getTryTimeout());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        client.close();
    }
}
