// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.eventhubs.implementation.AmqpReceiveLink;
import com.azure.eventhubs.implementation.AmqpResponseMapper;
import com.azure.eventhubs.implementation.AmqpSendLink;
import com.azure.eventhubs.implementation.ConnectionParameters;
import com.azure.eventhubs.implementation.EventHubConnection;
import com.azure.eventhubs.implementation.EventHubManagementNode;
import com.azure.eventhubs.implementation.ManagementChannel;
import com.azure.eventhubs.implementation.ReactorConnection;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.eventhubs.implementation.ReactorProvider;
import com.azure.eventhubs.implementation.StringUtil;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main point of interaction with Azure Event Hubs, the client offers a connection to a specific Event Hub within
 * the Event Hubs namespace and offers operations for sending event data, receiving events, and inspecting the connected
 * Event Hub.
 */
public class EventHubClient implements Closeable {
    private static final String RECEIVER_ENTITY_PATH_FORMAT = "%s/ConsumerGroups/%s/Partitions/%s";
    private static final String SENDER_ENTITY_PATH_FORMAT = "%s/Partitions/%s";

    private final String connectionId;
    private final Mono<EventHubConnection> connectionMono;
    private final AtomicBoolean hasConnection = new AtomicBoolean(false);
    private final ConnectionParameters connectionParameters;
    private final String eventHubPath;
    private final String host;
    private final EventSenderOptions defaultSenderOptions;
    private final EventReceiverOptions defaultReceiverOptions;

    EventHubClient(ConnectionParameters connectionParameters, ReactorProvider provider, ReactorHandlerProvider handlerProvider) {
        Objects.requireNonNull(connectionParameters);
        Objects.requireNonNull(provider);
        Objects.requireNonNull(handlerProvider);

        this.connectionParameters = connectionParameters;
        this.eventHubPath = connectionParameters.credentials().eventHubPath();
        this.host = connectionParameters.credentials().endpoint().getHost();
        this.connectionId = StringUtil.getRandomString("MF");
        this.connectionMono = Mono.fromCallable(() -> {
            return (EventHubConnection) new ReactorConnection(connectionId, connectionParameters, provider, handlerProvider, new ResponseMapper());
        }).doOnSubscribe(c -> hasConnection.set(true))
            .cache();

        this.defaultSenderOptions = new EventSenderOptions()
            .retry(connectionParameters.retryPolicy())
            .timeout(connectionParameters.timeout());
        this.defaultReceiverOptions = new EventReceiverOptions()
            .retry(connectionParameters.retryPolicy())
            .scheduler(connectionParameters.scheduler());
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
        return createSender(defaultSenderOptions);
    }

    /**
     * Creates a sender that can push events to an Event Hub. If
     * {@link EventSenderOptions#partitionId() options.partitionId()} is specified, then the events are routed to that
     * specific partition. Otherwise, events are automatically routed to an available partition.
     *
     * @param options The set of options to apply when creating the sender.
     * @return A new {@link EventSender}.
     * @throws NullPointerException if {@code options} is {@code null}.
     */
    public EventSender createSender(EventSenderOptions options) {
        Objects.requireNonNull(options);

        final EventSenderOptions clonedOptions = options.clone();
        if (clonedOptions.timeout() == null) {
            clonedOptions.timeout(connectionParameters.timeout());
        }
        if (clonedOptions.retry() == null) {
            clonedOptions.retry(connectionParameters.retryPolicy());
        }

        final String entityPath;
        final String linkName;

        if (ImplUtils.isNullOrEmpty(options.partitionId())) {
            entityPath = eventHubPath;
            linkName = StringUtil.getRandomString("EC");
        } else {
            entityPath = String.format(Locale.US, SENDER_ENTITY_PATH_FORMAT, eventHubPath, options.partitionId());
            linkName = StringUtil.getRandomString("PS");
        }

        final Mono<AmqpSendLink> amqpLinkMono = connectionMono.flatMap(connection -> connection.createSession(entityPath)
            .flatMap(session -> session.createSender(linkName, entityPath, clonedOptions.timeout(), clonedOptions.retry())
                .cast(AmqpSendLink.class)))
            .publish(x -> x);

        return new EventSender(amqpLinkMono, clonedOptions);
    }

    /**
     * Creates a receiver that listens to the Event Hub {@code partitionId} starting from the moment it was created. The
     * consumer group used is the {@link EventReceiverOptions#DEFAULT_CONSUMER_GROUP_NAME} consumer group.
     *
     * @param partitionId The identifier of the Event Hub partition.
     * @return An new {@link EventReceiver} that receives events from the partition at the given position.
     */
    public EventReceiver createReceiver(String partitionId) {
        return createReceiver(partitionId, defaultReceiverOptions);
    }

    /**
     * Creates a receiver that listens to the Event Hub {@code partitionId} at the given {@link EventPosition} with the
     * provided options.
     *
     * @param partitionId The identifier of the Event Hub partition.
     * @param options Additional options for the receiver.
     * @return An new {@link EventReceiver} that receives events from the partition with all configured {@link EventReceiverOptions}.
     * @throws NullPointerException if {@code partitionId} or {@code options} is {@code null}.
     */
    public EventReceiver createReceiver(String partitionId, EventReceiverOptions options) {
        Objects.requireNonNull(partitionId);
        Objects.requireNonNull(options);

        final EventReceiverOptions clonedOptions = options.clone();
        if (clonedOptions.scheduler() == null) {
            clonedOptions.scheduler(connectionParameters.scheduler());
        }
        if (clonedOptions.retry() == null) {
            clonedOptions.retry(connectionParameters.retryPolicy());
        }

        final String linkName = StringUtil.getRandomString("PR");
        final String entityPath = String.format(Locale.US, RECEIVER_ENTITY_PATH_FORMAT, eventHubPath, options.consumerGroup(), partitionId);

        final Mono<AmqpReceiveLink> receiveLinkMono = connectionMono.flatMap(connection -> connection.createSession(entityPath)
            .flatMap(session -> session.createReceiver(linkName, entityPath, connectionParameters.timeout(), clonedOptions.retry())
                .cast(AmqpReceiveLink.class)))
            .publish(x -> x);

        return new EventReceiver(receiveLinkMono, clonedOptions, connectionParameters.timeout());
    }

    /**
     * Closes and disposes of connection to service. Any {@link EventReceiver EventReceivers} and
     * {@link EventSender EventSenders} created with this instance will have their connections closed.
     */
    @Override
    public void close() {
        if (hasConnection.getAndSet(false)) {
            try {
                final AmqpConnection connection = connectionMono.block(connectionParameters.timeout());
                if (connection != null) {
                    connection.close();
                }
            } catch (IOException exception) {
                throw new AmqpException(false, "Unable to close connection to service", exception);
            }
        }
    }

    private static class ResponseMapper implements AmqpResponseMapper {
        @Override
        public EventHubProperties toEventHubProperties(Map<?, ?> amqpBody) {
            return new EventHubProperties(
                (String) amqpBody.get(ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY),
                ((Date) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_CREATED_AT)).toInstant(),
                (String[]) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_PARTITION_IDS));
        }

        @Override
        public PartitionProperties toPartitionProperties(Map<?, ?> amqpBody) {
            return new PartitionProperties(
                (String) amqpBody.get(ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY),
                (String) amqpBody.get(ManagementChannel.MANAGEMENT_PARTITION_NAME_KEY),
                (Long) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_BEGIN_SEQUENCE_NUMBER),
                (Long) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER),
                (String) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET),
                ((Date) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC)).toInstant(),
                (Boolean) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_PARTITION_IS_EMPTY));
        }
    }
}
