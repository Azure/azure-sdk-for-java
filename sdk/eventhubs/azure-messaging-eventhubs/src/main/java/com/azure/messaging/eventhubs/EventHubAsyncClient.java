// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.util.Objects;

/**
 * An <strong>asynchronous</strong> client that is the main point of interaction with Azure Event Hubs. It connects to a
 * specific Event Hub and allows operations for sending event data, receiving data, and inspecting the Event Hub's
 * metadata.
 *
 * @see EventHubClientBuilder
 * @see EventHubClient See EventHubClient to communicate with an Event Hub using a synchronous client.
 * @see <a href="https://docs.microsoft.com/Azure/event-hubs/event-hubs-about">About Azure Event Hubs</a>
 */
class EventHubAsyncClient implements Closeable {
    private final ClientLogger logger = new ClientLogger(EventHubAsyncClient.class);
    private final MessageSerializer messageSerializer;
    private final EventHubConnection connection;
    private final boolean isSharedConnection;
    private final EventHubConsumerOptions defaultConsumerOptions;
    private final TracerProvider tracerProvider;

    EventHubAsyncClient(EventHubConnection connection, TracerProvider tracerProvider,
        MessageSerializer messageSerializer, boolean isSharedConnection) {
        this.tracerProvider = Objects.requireNonNull(tracerProvider, "'tracerProvider' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.connection = Objects.requireNonNull(connection, "'connection' cannot be null.");
        this.isSharedConnection = isSharedConnection;
        this.defaultConsumerOptions = new EventHubConsumerOptions();
    }

    /**
     * Gets the fully qualified namespace of this Event Hub.
     *
     * @return The fully qualified namespace of this Event Hub.
     */
    String getFullyQualifiedNamespace() {
        return connection.getFullyQualifiedNamespace();
    }

    /**
     * Gets the Event Hub name this client interacts with.
     *
     * @return The Event Hub name this client interacts with.
     */
    String getEventHubName() {
        return connection.getEventHubName();
    }

    /**
     * Gets information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    Mono<EventHubProperties> getProperties() {
        return connection.getManagementNode().flatMap(EventHubManagementNode::getEventHubProperties);
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return A Flux of identifiers for the partitions of an Event Hub.
     */
    Flux<String> getPartitionIds() {
        return getProperties().flatMapMany(properties -> Flux.fromArray(properties.getPartitionIds()));
    }

    /**
     * Retrieves information about a specific partition for an Event Hub, including elements that describe the available
     * events in the partition event stream.
     *
     * @param partitionId The unique identifier of a partition associated with the Event Hub.
     * @return The set of information for the requested partition under the Event Hub this client is associated with.
     */
    Mono<PartitionProperties> getPartitionProperties(String partitionId) {
        return connection.getManagementNode().flatMap(node -> node.getPartitionProperties(partitionId));
    }

    /**
     * Creates an Event Hub producer responsible for transmitting {@link EventData} to the Event Hub, grouped together
     * in batches.
     *
     * @return A new {@link EventHubProducerAsyncClient}.
     */
    EventHubProducerAsyncClient createProducer() {
        return new EventHubProducerAsyncClient(connection.getFullyQualifiedNamespace(), getEventHubName(), connection,
            connection.getRetryOptions(), tracerProvider, messageSerializer, isSharedConnection);
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
     * context of this group. The name of the consumer group that is created by default is {@link
     * EventHubClientBuilder#DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @return A new {@link EventHubConsumerAsyncClient} that receives events from the partition at the given position.
     * @throws NullPointerException If {@code eventPosition}, or {@code options} is {@code null}.
     * @throws IllegalArgumentException If {@code consumerGroup} or {@code partitionId} is {@code null} or an empty
     * string.
     */
    EventHubConsumerAsyncClient createConsumer(String consumerGroup, EventPosition eventPosition) {
        return createConsumer(consumerGroup, eventPosition, defaultConsumerOptions);
    }

    /**
     * Creates an Event Hub consumer responsible for reading {@link EventData} from a specific Event Hub partition, as a
     * member of the configured consumer group, and begins reading events from the specified {@code eventPosition}.
     *
     * <p>
     * A consumer may be exclusive, which asserts ownership over the partition for the consumer group to ensure that
     * only one consumer from that group is reading the from the partition. These exclusive consumers are sometimes
     * referred to as "Epoch Consumers."
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
     * context of this group. The name of the consumer group that is created by default is
     * {@link EventHubClientBuilder#DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @param options The set of options to apply when creating the consumer.
     * @return An new {@link EventHubConsumerAsyncClient} that receives events from the partition with all configured
     * {@link EventHubConsumerOptions}.
     * @throws NullPointerException If {@code eventPosition}, {@code consumerGroup}, {@code partitionId}, or
     * {@code options} is {@code null}.
     * @throws IllegalArgumentException If {@code consumerGroup} or {@code partitionId} is an empty string.
     */
    EventHubConsumerAsyncClient createConsumer(String consumerGroup, EventPosition eventPosition,
        EventHubConsumerOptions options) {
        Objects.requireNonNull(eventPosition, "'eventPosition' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");
        Objects.requireNonNull(consumerGroup, "'consumerGroup' cannot be null.");

        if (consumerGroup.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'consumerGroup' cannot be an empty string."));
        }

        final EventHubConsumerOptions clonedOptions = options.clone();

        return new EventHubConsumerAsyncClient(connection.getFullyQualifiedNamespace(), getEventHubName(),
            connection, messageSerializer, consumerGroup, eventPosition, clonedOptions, isSharedConnection);
    }

    /**
     * Closes and disposes of connection to service. Any {@link EventHubConsumerAsyncClient EventHubConsumers} and
     * {@link EventHubProducerAsyncClient EventHubProducers} created with this instance will have their connections
     * closed.
     */
    @Override
    public void close() {
        connection.close();
    }
}
