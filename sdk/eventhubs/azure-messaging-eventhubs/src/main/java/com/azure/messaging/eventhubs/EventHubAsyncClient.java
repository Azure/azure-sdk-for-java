// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
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
    private final TracerProvider tracerProvider;

    EventHubAsyncClient(EventHubConnection connection, TracerProvider tracerProvider,
        MessageSerializer messageSerializer, boolean isSharedConnection) {
        this.tracerProvider = Objects.requireNonNull(tracerProvider, "'tracerProvider' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.connection = Objects.requireNonNull(connection, "'connection' cannot be null.");
        this.isSharedConnection = isSharedConnection;
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
        return getProperties().flatMapMany(properties -> Flux.fromIterable(properties.getPartitionIds()));
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
     * Creates an Event Hub consumer responsible for reading {@link EventData} from a specific Event Hub, as a
     * member of the configured consumer group.
     *
     * @param consumerGroup The name of the consumer group this consumer is associated with. Events are read in the
     * context of this group. The name of the consumer group that is created by default is
     * {@link EventHubClientBuilder#DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     * @param prefetchCount The set of options to apply when creating the consumer.
     * @return An new {@link EventHubConsumerAsyncClient} that receives events from the Event Hub.
     * @throws NullPointerException If {@code consumerGroup} is {@code null}.
     * @throws IllegalArgumentException If {@code consumerGroup} is an empty string.
     */
    EventHubConsumerAsyncClient createConsumer(String consumerGroup, int prefetchCount) {
        Objects.requireNonNull(consumerGroup, "'consumerGroup' cannot be null.");

        if (consumerGroup.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'consumerGroup' cannot be an empty string."));
        }

        return new EventHubConsumerAsyncClient(connection.getFullyQualifiedNamespace(), getEventHubName(),
            connection, messageSerializer, consumerGroup, prefetchCount, isSharedConnection);
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
