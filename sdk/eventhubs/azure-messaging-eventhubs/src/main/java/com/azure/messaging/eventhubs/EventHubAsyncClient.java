// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsConsumerInstrumentation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

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
    private static final ClientLogger LOGGER = new ClientLogger(EventHubAsyncClient.class);
    private final MessageSerializer messageSerializer;
    private final EventHubConnectionProcessor connectionProcessor;
    private final Scheduler scheduler;
    private final boolean isSharedConnection;
    private final Runnable onClientClose;
    private final String identifier;
    private final Tracer tracer;
    private final Meter meter;

    EventHubAsyncClient(EventHubConnectionProcessor connectionProcessor, MessageSerializer messageSerializer,
        Scheduler scheduler, boolean isSharedConnection, Runnable onClientClose, String identifier, Meter meter, Tracer tracer) {
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor,
            "'connectionProcessor' cannot be null.");
        this.scheduler = Objects.requireNonNull(scheduler, "'scheduler' cannot be null");
        this.onClientClose = Objects.requireNonNull(onClientClose, "'onClientClose' cannot be null.");

        this.isSharedConnection = isSharedConnection;
        this.identifier = identifier;
        this.tracer = tracer;
        this.meter = meter;
    }

    /**
     * Gets the fully qualified namespace of this Event Hub.
     *
     * @return The fully qualified namespace of this Event Hub.
     */
    String getFullyQualifiedNamespace() {
        return connectionProcessor.getFullyQualifiedNamespace();
    }

    /**
     * Gets the Event Hub name this client interacts with.
     *
     * @return The Event Hub name this client interacts with.
     */
    String getEventHubName() {
        return connectionProcessor.getEventHubName();
    }

    /**
     * Gets information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    Mono<EventHubProperties> getProperties() {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode())
            .flatMap(EventHubManagementNode::getEventHubProperties);
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
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode())
            .flatMap(node -> node.getPartitionProperties(partitionId));
    }

    /**
     * Creates an Event Hub producer responsible for transmitting {@link EventData} to the Event Hub, grouped together
     * in batches.
     *
     * @return A new {@link EventHubProducerAsyncClient}.
     */
    EventHubProducerAsyncClient createProducer() {
        EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(tracer, meter, connectionProcessor.getFullyQualifiedNamespace(), connectionProcessor.getEventHubName());
        return new EventHubProducerAsyncClient(connectionProcessor.getFullyQualifiedNamespace(), getEventHubName(),
            connectionProcessor, connectionProcessor.getRetryOptions(), messageSerializer, scheduler,
            isSharedConnection, onClientClose, identifier, instrumentation);
    }

    /**
     * Creates an Event Hub consumer responsible for reading {@link EventData} from a specific Event Hub, as a
     * member of the configured consumer group.
     *
     * @param consumerGroup The name of the consumer group this consumer is associated with. Events are read in the
     * context of this group. The name of the consumer group that is created by default is
     * {@link EventHubClientBuilder#DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     * @param prefetchCount The number of events to queue locally.
     * @return An new {@link EventHubConsumerAsyncClient} that receives events from the Event Hub.
     * @throws NullPointerException If {@code consumerGroup} is {@code null}.
     * @throws IllegalArgumentException If {@code consumerGroup} is an empty string.
     */
    EventHubConsumerAsyncClient createConsumer(String consumerGroup, int prefetchCount, boolean isSync) {
        Objects.requireNonNull(consumerGroup, "'consumerGroup' cannot be null.");

        if (consumerGroup.isEmpty()) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'consumerGroup' cannot be an empty string."));
        }

        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer, meter,
            connectionProcessor.getFullyQualifiedNamespace(), connectionProcessor.getEventHubName(), consumerGroup, isSync);

        return new EventHubConsumerAsyncClient(connectionProcessor.getFullyQualifiedNamespace(), getEventHubName(),
            connectionProcessor, messageSerializer, consumerGroup, prefetchCount, isSharedConnection,
            onClientClose, identifier, instrumentation);
    }

    /**
     * Closes and disposes of connection to service. Any {@link EventHubConsumerAsyncClient EventHubConsumers} and
     * {@link EventHubProducerAsyncClient EventHubProducers} created with this instance will have their connections
     * closed.
     */
    @Override
    public void close() {
        connectionProcessor.dispose();
    }

    /**
     * Gets the client identifier.
     *
     * @return The unique identifier string for current client.
     */
    public String getIdentifier() {
        return identifier;
    }
}
