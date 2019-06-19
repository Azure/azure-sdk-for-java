// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.eventhubs.implementation.AmqpReceiveLink;
import com.azure.eventhubs.implementation.AmqpResponseMapper;
import com.azure.eventhubs.implementation.AmqpSendLink;
import com.azure.eventhubs.implementation.ConnectionOptions;
import com.azure.eventhubs.implementation.EventHubConnection;
import com.azure.eventhubs.implementation.EventHubManagementNode;
import com.azure.eventhubs.implementation.EventHubSession;
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
    private final ConnectionOptions connectionOptions;
    private final String eventHubPath;
    private final EventHubProducerOptions defaultSenderOptions;
    private final EventHubConsumerOptions defaultReceiverOptions;

    EventHubClient(ConnectionOptions connectionOptions, ReactorProvider provider, ReactorHandlerProvider handlerProvider) {
        Objects.requireNonNull(connectionOptions);
        Objects.requireNonNull(provider);
        Objects.requireNonNull(handlerProvider);

        this.connectionOptions = connectionOptions;
        this.eventHubPath = connectionOptions.eventHubPath();
        this.connectionId = StringUtil.getRandomString("MF");
        this.connectionMono = Mono.fromCallable(() -> {
            return (EventHubConnection) new ReactorConnection(connectionId, connectionOptions, provider, handlerProvider, new ResponseMapper());
        }).doOnSubscribe(c -> hasConnection.set(true))
            .cache();

        this.defaultSenderOptions = new EventHubProducerOptions()
            .retry(connectionOptions.retryPolicy())
            .timeout(connectionOptions.timeout());
        this.defaultReceiverOptions = new EventHubConsumerOptions()
            .retry(connectionOptions.retryPolicy())
            .scheduler(connectionOptions.scheduler());
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
     * Creates an Event Hub producer responsible for transmitting {@link EventData} to the Event Hub, grouped together
     * in batches. Event data is automatically routed to an available partition.
     *
     * @return A new {@link EventHubProducer}.
     */
    public EventHubProducer createProducer() {
        return createProducer(defaultSenderOptions);
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
        Objects.requireNonNull(options);

        final EventHubProducerOptions clonedOptions = options.clone();
        if (clonedOptions.timeout() == null) {
            clonedOptions.timeout(connectionOptions.timeout());
        }
        if (clonedOptions.retry() == null) {
            clonedOptions.retry(connectionOptions.retryPolicy());
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

        return new EventHubProducer(amqpLinkMono, clonedOptions);
    }

    /**
     * Creates an Event Hub consumer responsible for reading {@link EventData} from a specific Event Hub partition,
     * as a member of the {@link EventHubConsumerOptions#DEFAULT_CONSUMER_GROUP_NAME default consumer group}, and begins
     * reading events from the specified {@code eventPosition}.
     *
     * The consumer created is non-exclusive, allowing multiple consumers from the same consumer group to be actively
     * reading events from the partition. These non-exclusive consumers are sometimes referred to as "Non-epoch
     * Consumers".
     *
     * @param partitionId The identifier of the Event Hub partition.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @return An new {@link EventHubConsumer} that receives events from the partition at the given position.
     */
    public EventHubConsumer createConsumer(String partitionId, EventPosition eventPosition) {
        return createConsumer(partitionId, eventPosition, defaultReceiverOptions);
    }

    /**
     * Creates an Event Hub consumer responsible for reading {@link EventData} from a specific Event Hub partition,
     * as a member of the configured consumer group, and begins reading events from the specified {@code eventPosition}.
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
     * Designating a consumer as exclusive may be specified in the {@code options}, by setting
     * {@link EventHubConsumerOptions#exclusiveReceiverPriority(Long)} to a non-null value. By default, consumers are
     * created as non-exclusive.
     * </p>
     *
     * @param partitionId The identifier of the Event Hub partition from which events will be received.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @param options The set of options to apply when creating the consumer.
     * @return An new {@link EventHubConsumer} that receives events from the partition with all configured
     *         {@link EventHubConsumerOptions}.
     * @throws NullPointerException if {@code partitionId}, {@code eventPosition}, or {@code options} is {@code null}.
     */
    public EventHubConsumer createConsumer(String partitionId, EventPosition eventPosition, EventHubConsumerOptions options) {
        Objects.requireNonNull(partitionId);
        Objects.requireNonNull(eventPosition);
        Objects.requireNonNull(options);

        final EventHubConsumerOptions clonedOptions = options.clone();
        if (clonedOptions.scheduler() == null) {
            clonedOptions.scheduler(connectionOptions.scheduler());
        }
        if (clonedOptions.retry() == null) {
            clonedOptions.retry(connectionOptions.retryPolicy());
        }

        final String linkName = StringUtil.getRandomString("PR");
        final String entityPath = String.format(Locale.US, RECEIVER_ENTITY_PATH_FORMAT, eventHubPath, options.consumerGroup(), partitionId);

        final Mono<AmqpReceiveLink> receiveLinkMono = connectionMono.flatMap(connection -> connection.createSession(entityPath))
            .cast(EventHubSession.class)
            .flatMap(session -> {
                final Long priority = options.exclusiveReceiverPriority().isPresent()
                    ? options.exclusiveReceiverPriority().get()
                    : null;

                return session.createConsumer(linkName, entityPath, eventPosition.getExpression(), connectionOptions.timeout(),
                    clonedOptions.retry(), priority, options.keepPartitionInformationUpdated(), options.identifier());
            })
            .cast(AmqpReceiveLink.class);

        return new EventHubConsumer(receiveLinkMono, clonedOptions, connectionOptions.timeout());
    }

    /**
     * Closes and disposes of connection to service. Any {@link EventHubConsumer EventHubConsumers} and
     * {@link EventHubProducer EventHubProducers} created with this instance will have their connections closed.
     */
    @Override
    public void close() {
        if (hasConnection.getAndSet(false)) {
            try {
                final AmqpConnection connection = connectionMono.block(connectionOptions.timeout());
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
