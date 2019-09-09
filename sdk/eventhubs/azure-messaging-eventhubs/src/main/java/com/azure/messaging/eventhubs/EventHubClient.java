// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.implementation.annotation.ReturnType;
import com.azure.core.implementation.annotation.ServiceClient;
import com.azure.core.implementation.annotation.ServiceMethod;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.implementation.ConnectionOptions;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;

import java.io.Closeable;
import java.time.Duration;
import java.util.Objects;

import static com.azure.messaging.eventhubs.EventHubErrorCodeStrings.getErrorString;

/**
 * A <strong>synchronous</strong> client that is the main point of interaction with Azure Event Hubs. It connects to a
 * specific Event Hub and allows operations for sending event data, receiving data, and inspecting the Event Hub's
 * metadata.
 *
 * <p>
 * Instantiated through {@link EventHubClientBuilder}.
 * </p>
 *
 * <p>
 * <strong>Creating a synchronous {@link EventHubClient} using an Event Hub instance connection string</strong>
 * </p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubclient.instantiation}
 *
 * @see EventHubClientBuilder
 * @see EventHubAsyncClient To communicate with Event Hub using an asynchronous client.
 * @see <a href="https://docs.microsoft.com/Azure/event-hubs/event-hubs-about">About Azure Event Hubs</a>
 */
@ServiceClient(builder = EventHubClientBuilder.class)
public class EventHubClient implements Closeable {
    private final EventHubAsyncClient client;
    private final RetryOptions retry;
    private final EventHubProducerOptions defaultProducerOptions;
    private final EventHubConsumerOptions defaultConsumerOptions;

    EventHubClient(EventHubAsyncClient client, ConnectionOptions connectionOptions) {
        Objects.requireNonNull(connectionOptions,
            String.format(getErrorString(EventHubErrorCodeStrings.CANNOT_BE_NULL), "connectionOptions"));

        this.client = Objects.requireNonNull(client,
            String.format(getErrorString(EventHubErrorCodeStrings.CANNOT_BE_NULL), "client"));
        this.retry = connectionOptions.retry();
        this.defaultProducerOptions = new EventHubProducerOptions()
            .retry(connectionOptions.retry());
        this.defaultConsumerOptions = new EventHubConsumerOptions()
            .retry(connectionOptions.retry())
            .scheduler(connectionOptions.scheduler());
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EventHubProperties getProperties() {
        return client.getProperties().block(retry.tryTimeout());
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
        return client.getPartitionProperties(partitionId).block(retry.tryTimeout());
    }

    /**
     * Creates an Event Hub producer responsible for transmitting {@link EventData} to the Event Hub, grouped together
     * in batches. Event data is automatically routed to an available partition.
     *
     * @return A new {@link EventHubProducer}.
     */
    public EventHubProducer createProducer() {
        return createProducer(defaultProducerOptions);
    }

    /**
     * Creates an Event Hub producer responsible for transmitting {@link EventData} to the Event Hub, grouped together
     * in batches. If {@link EventHubProducerOptions#partitionId() options.partitionId()} is not {@code null}, the
     * events are routed to that specific partition. Otherwise, events are automatically routed to an available
     * partition.
     *
     * @param options The set of options to apply when creating the producer.
     * @return A new {@link EventHubProducer}.
     * @throws NullPointerException if {@code options} is {@code null}.
     */
    public EventHubProducer createProducer(EventHubProducerOptions options) {
        Objects.requireNonNull(options,
            String.format(getErrorString(EventHubErrorCodeStrings.CANNOT_BE_NULL), "options"));

        final EventHubAsyncProducer producer = client.createProducer(options);

        final Duration tryTimeout = options.retry() != null && options.retry().tryTimeout() != null
            ? options.retry().tryTimeout()
            : defaultProducerOptions.retry().tryTimeout();

        return new EventHubProducer(producer, tryTimeout);
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
     *     EventHubAsyncClient#DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     * @param partitionId The identifier of the Event Hub partition.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @return A new {@link EventHubConsumer} that receives events from the partition at the given position.
     * @throws NullPointerException If {@code eventPosition}, {@code consumerGroup}, {@code partitionId}, or
     *     {@code options} is {@code null}.
     * @throws IllegalArgumentException If {@code consumerGroup} or {@code partitionId} is an empty string.
     */
    public EventHubConsumer createConsumer(String consumerGroup, String partitionId, EventPosition eventPosition) {
        final EventHubAsyncConsumer consumer = client.createConsumer(consumerGroup, partitionId, eventPosition);
        return new EventHubConsumer(consumer, defaultConsumerOptions.retry().tryTimeout());
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
     * EventHubConsumerOptions#ownerLevel(Long)} to a non-null value. By default, consumers are created as
     * non-exclusive.
     * </p>
     *
     * @param consumerGroup The name of the consumer group this consumer is associated with. Events are read in the
     *     context of this group. The name of the consumer group that is created by default is {@link
     *     EventHubAsyncClient#DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     * @param partitionId The identifier of the Event Hub partition from which events will be received.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @param options The set of options to apply when creating the consumer.
     * @return An new {@link EventHubConsumer} that receives events from the partition with all configured {@link
     *     EventHubConsumerOptions}.
     * @throws NullPointerException If {@code eventPosition}, {@code consumerGroup}, {@code partitionId}, or
     *     {@code options} is {@code null}.
     * @throws IllegalArgumentException If {@code consumerGroup} or {@code partitionId} is an empty string.
     */
    public EventHubConsumer createConsumer(String consumerGroup, String partitionId, EventPosition eventPosition,
                                           EventHubConsumerOptions options) {
        final EventHubAsyncConsumer consumer =
            client.createConsumer(consumerGroup, partitionId, eventPosition, options);
        final Duration timeout = options.retry() == null || options.retry().tryTimeout() == null
            ? defaultConsumerOptions.retry().tryTimeout()
            : options.retry().tryTimeout();

        return new EventHubConsumer(consumer, timeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        client.close();
    }
}
