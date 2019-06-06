// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.eventhubs.implementation.EventHubConnection;
import com.azure.eventhubs.implementation.EventHubManagementNode;
import com.azure.eventhubs.implementation.ReactorConnection;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.eventhubs.implementation.ReactorProvider;
import com.azure.eventhubs.implementation.StringUtil;
import com.azure.eventhubs.implementation.TokenProvider;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main point of interaction with Azure Event Hubs, the client offers a connection to a specific Event Hub within
 * the Event Hubs namespace and offers operations for sending event data, receiving events, and inspecting the connected
 * Event Hub.
 */
public class EventHubClient implements Closeable {
    private final String connectionId;
    private final Mono<EventHubConnection> connectionMono;
    private final String host;
    private final AtomicBoolean hasConnection = new AtomicBoolean(false);

    private final Duration timeout;
    private final String eventHubName;
    private final Scheduler scheduler;
    private final TokenProvider tokenProvider;

    EventHubClient(ConnectionParameters connectionParameters, ReactorProvider provider, ReactorHandlerProvider handlerProvider) {
        Objects.requireNonNull(connectionParameters, "'connectionParameters' is null");
        this.eventHubName = connectionParameters.credentials().eventHubPath();
        this.host = connectionParameters.credentials().endpoint().getHost();
        this.timeout = connectionParameters.timeout();
        this.tokenProvider = connectionParameters.tokenProvider();
        this.scheduler = connectionParameters.scheduler();
        this.connectionId = StringUtil.getRandomString("MF");
        this.connectionMono = Mono.fromCallable(() -> (EventHubConnection) new ReactorConnection(connectionId, host, eventHubName, this.tokenProvider, provider, handlerProvider, this.scheduler))
            .doOnSubscribe(c -> hasConnection.set(true))
            .cache();
    }

    /**
     * Creates a builder that can configure options for the {@link EventHubClient} before creating an instance of it.
     *
     * @return A new {@link EventHubClientBuilder} to create an EventHubClient from.
     */
    public static EventHubClientBuilder builder() {
        return new EventHubClientBuilder();
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    public Mono<EventHubProperties> getProperties() {
        return connectionMono.flatMap(connection -> connection.getManagementNode().flatMap(EventHubManagementNode::getEventHubProperties));
    }

    /**
     * Retrieves the set of identifiers for the partitions of an Event Hub.
     *
     * @return The set of identifiers for the partitions of an Event Hub.
     */
    public Mono<String[]> getPartitionIds() {
        return getProperties().map(EventHubProperties::partitionIds);
    }

    /**
     * Retrieves information about a specific partition for an Event Hub, including elements that describe the available
     * events in the partition event stream.
     *
     * @param partitionId The unique identifier of a partition associated with the Event Hub.
     * @return The set of information for the requested partition under the Event Hub this client is associated with.
     */
    public Mono<PartitionProperties> getPartitionProperties(String partitionId) {
        return connectionMono.flatMap(
            connection -> connection.getManagementNode().flatMap(node -> {
                return node.getPartitionProperties(partitionId);
            }));
    }

    /**
     * Creates a sender that transmits events to Event Hub. Event data is automatically routed to an available
     * partition.
     *
     * @return A new {@link EventSender}.
     */
    public EventSender createSender() {
        return new EventSender();
    }

    /**
     * Creates a sender that can push events to an Event Hub. If
     * {@link SenderOptions#partitionId() options.partitionId()} is specified, then the events are routed to that
     * specific partition. Otherwise, events are automatically routed to an available partition.
     *
     * @param options The set of options to apply when creating the sender.
     * @return A new {@link EventSender}.
     */
    public EventSender createSender(SenderOptions options) {
        return new EventSender(options);
    }

    /**
     * Creates a receiver that listens to the Event Hub {@code partitionId} starting from the moment it was created. The
     * consumer group used is the {@link ReceiverOptions#DEFAULT_CONSUMER_GROUP_NAME} consumer group.
     *
     * @param partitionId The identifier of the Event Hub partition.
     * @return An new {@link EventReceiver} that receives events from the partition at the given position.
     */
    public EventReceiver createReceiver(String partitionId) {
        return new EventReceiver();
    }

    /**
     * Creates a receiver that listens to the Event Hub {@code partitionId} at the given {@link EventPosition} with the
     * provided options.
     *
     * @param partitionId The identifier of the Event Hub partition.
     * @param options Additional options for the receiver.
     * @return An new {@link EventReceiver} that receives events from the partition at the given position.
     */
    public EventReceiver createReceiver(String partitionId, ReceiverOptions options) {
        return new EventReceiver();
    }

    /**
     * Closes and disposes of connection to service. Any {@link EventReceiver EventReceivers} and
     * {@link EventSender EventSenders} created with this instance will have their connections closed.
     */
    @Override
    public void close() {
        if (hasConnection.getAndSet(false)) {
            try {
                final AmqpConnection connection = connectionMono.block(timeout);
                if (connection != null) {
                    connection.close();
                }
            } catch (IOException exception) {
                throw new AmqpException(false, "Unable to close connection to service", exception);
            }
        }
    }
}
