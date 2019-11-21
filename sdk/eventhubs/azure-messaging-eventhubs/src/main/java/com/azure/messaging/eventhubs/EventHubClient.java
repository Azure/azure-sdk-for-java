// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.IterableStream;

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
    private final AmqpRetryOptions retry;

    EventHubClient(EventHubAsyncClient client, AmqpRetryOptions retry) {
        this.client = Objects.requireNonNull(client, "'client' cannot be null.");
        this.retry = retry;
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    EventHubProperties getProperties() {
        return client.getProperties().block(retry.getTryTimeout());
    }

    /**
     * Retrieves the identifiers for all the partitions of an Event Hub.
     *
     * @return The identifiers for all partitions of an Event Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    IterableStream<String> getPartitionIds() {
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
    PartitionProperties getPartitionProperties(String partitionId) {
        return client.getPartitionProperties(partitionId).block(retry.getTryTimeout());
    }

    /**
     * Creates an Event Hub producer responsible for transmitting {@link EventData} to the Event Hub, grouped together
     * in batches. Event data is automatically routed to an available partition.
     *
     * @return A new {@link EventHubProducerClient}.
     */
    EventHubProducerClient createProducer() {
        final EventHubProducerAsyncClient producer = client.createProducer();
        return new EventHubProducerClient(producer, retry.getTryTimeout());
    }

    /**
     * Creates an Event Hub consumer responsible for reading {@link EventData} from a specific Event Hub, as a member of
     * the configured consumer group.
     *
     * @param consumerGroup The name of the consumer group this consumer is associated with. Events are read in the
     *     context of this group. The name of the consumer group that is created by default is {@link
     *     EventHubClientBuilder#DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     * @param prefetchCount The set of options to apply when creating the consumer.
     * @return An new {@link EventHubConsumerClient} that receives events.
     * @throws NullPointerException If {@code consumerGroup} is null.
     * @throws IllegalArgumentException If {@code consumerGroup} is an empty string.
     */
    EventHubConsumerClient createConsumer(String consumerGroup, int prefetchCount) {
        final EventHubConsumerAsyncClient consumer = client.createConsumer(consumerGroup, prefetchCount);

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
