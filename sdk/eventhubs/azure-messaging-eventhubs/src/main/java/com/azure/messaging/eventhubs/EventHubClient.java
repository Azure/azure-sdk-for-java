// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;

import java.io.Closeable;
import java.util.Objects;

/**
 * Package-private.
 *
 * A <strong>synchronous</strong> client that is the main point of interaction with Azure Event Hubs. It connects to a
 * specific Event Hub and allows operations for sending event data, receiving data, and inspecting the Event Hub's
 * metadata.
 *
 * @see EventHubClientBuilder
 */
class EventHubClient implements Closeable {
    private final EventHubConnection eventHubConnection;
    private final RetryOptions retry;
    private final boolean isSharedConnection;

    EventHubClient(EventHubConnection eventHubConnection, RetryOptions retryOptions, boolean isSharedConnection) {
        this.eventHubConnection = Objects.requireNonNull(eventHubConnection, "'eventHubConnection' cannot be null.");
        this.retry = retryOptions;
        this.isSharedConnection = isSharedConnection;
    }

    /**
     * Creates an Event Hub producer responsible for transmitting {@link EventData} to the Event Hub, grouped together
     * in batches. Event data is automatically routed to an available partition.
     *
     * @return A new {@link EventHubProducerClient}.
     */
    EventHubProducerClient createProducer() {
        final EventHubProducerAsyncClient producer = eventHubConnection.createProducer(isSharedConnection);
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
     * @param partitionId The identifier of the Event Hub partition.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @return A new {@link EventHubConsumer} that receives events from the partition at the given position.
     * @throws NullPointerException If {@code eventPosition}, {@code consumerGroup}, {@code partitionId}, or
     *     {@code options} is {@code null}.
     * @throws IllegalArgumentException If {@code consumerGroup} or {@code partitionId} is an empty string.
     */
    EventHubConsumer createConsumer(String consumerGroup, String partitionId, EventPosition eventPosition) {
        final EventHubAsyncConsumer consumer = eventHubConnection.createConsumer(consumerGroup, partitionId, eventPosition);
        return new EventHubConsumer(consumer, retry.getTryTimeout());
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
     * @param partitionId The identifier of the Event Hub partition from which events will be received.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @param options The set of options to apply when creating the consumer.
     * @return An new {@link EventHubConsumer} that receives events from the partition with all configured {@link
     *     EventHubConsumerOptions}.
     * @throws NullPointerException If {@code eventPosition}, {@code consumerGroup}, {@code partitionId}, or
     *     {@code options} is {@code null}.
     * @throws IllegalArgumentException If {@code consumerGroup} or {@code partitionId} is an empty string.
     */
    EventHubConsumer createConsumer(String consumerGroup, String partitionId, EventPosition eventPosition,
            EventHubConsumerOptions options) {
        final EventHubAsyncConsumer consumer =
            eventHubConnection.createConsumer(consumerGroup, partitionId, eventPosition, options);

        return new EventHubConsumer(consumer, retry.getTryTimeout());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        eventHubConnection.close();
    }
}
