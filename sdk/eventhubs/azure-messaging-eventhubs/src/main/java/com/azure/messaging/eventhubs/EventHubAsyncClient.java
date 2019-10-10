// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.RetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorContext;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.EventHubConnection;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.implementation.EventHubSession;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An <strong>asynchronous</strong> client that is the main point of interaction with Azure Event Hubs. It connects to a
 * specific Event Hub and allows operations for sending event data, receiving data, and inspecting the Event Hub's
 * metadata.
 *
 * <p>
 * Instantiated through {@link EventHubClientBuilder}.
 * </p>
 *
 * <p><strong>Creating an {@link EventHubAsyncClient} using an Event Hubs namespace connection string</strong></p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncclient.instantiation#string-string}
 *
 * <p><strong>Creating an {@link EventHubAsyncClient} using an Event Hub instance connection string</strong></p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncclient.instantiation#string}
 *
 * @see EventHubClientBuilder
 * @see EventHubClient See EventHubClient to communicate with an Event Hub using a synchronous client.
 * @see <a href="https://docs.microsoft.com/Azure/event-hubs/event-hubs-about">About Azure Event Hubs</a>
 */
@ServiceClient(builder = EventHubClientBuilder.class, isAsync = true)
public class EventHubAsyncClient implements Closeable {
    /**
     * The name of the default consumer group in the Event Hubs service.
     */
    public static final String DEFAULT_CONSUMER_GROUP_NAME = "$Default";

    private static final String RECEIVER_ENTITY_PATH_FORMAT = "%s/ConsumerGroups/%s/Partitions/%s";
    private static final String SENDER_ENTITY_PATH_FORMAT = "%s/Partitions/%s";

    private final ClientLogger logger = new ClientLogger(EventHubAsyncClient.class);
    private final MessageSerializer messageSerializer;
    private final Mono<EventHubConnection> connectionMono;
    private final AtomicBoolean hasConnection = new AtomicBoolean(false);
    private final ConnectionOptions connectionOptions;
    private final String eventHubName;
    private final EventHubProducerOptions defaultProducerOptions;
    private final EventHubConsumerOptions defaultConsumerOptions;
    private final TracerProvider tracerProvider;

    EventHubAsyncClient(ConnectionOptions connectionOptions, TracerProvider tracerProvider,
                        MessageSerializer messageSerializer, Mono<EventHubConnection> eventHubConnectionMono) {

        this.connectionOptions = Objects.requireNonNull(connectionOptions, "'connectionOptions' cannot be null.");
        this.tracerProvider = Objects.requireNonNull(tracerProvider, "'tracerProvider' cannot be null.");
        this.eventHubName = connectionOptions.getEntityPath();
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.connectionMono = Objects.requireNonNull(eventHubConnectionMono, "'eventHubConnectionMono' cannot be null.")
            .doOnSubscribe(c -> hasConnection.set(true))
            .cache();

        this.defaultProducerOptions = new EventHubProducerOptions()
            .setRetry(connectionOptions.getRetry());
        this.defaultConsumerOptions = new EventHubConsumerOptions()
            .setRetry(connectionOptions.getRetry())
            .setScheduler(connectionOptions.getScheduler());
    }

    /**
     * Gets the Event Hub name this client interacts with.
     *
     * @return The Event Hub name this client interacts with.
     */
    public String getEventHubName() {
        return eventHubName;
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EventHubProperties> getProperties() {
        return connectionMono
            .flatMap(connection -> connection
                .getManagementNode().flatMap(EventHubManagementNode::getEventHubProperties));
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return A Flux of identifiers for the partitions of an Event Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<String> getPartitionIds() {
        return getProperties().flatMapMany(properties -> Flux.fromArray(properties.getPartitionIds()));
    }

    /**
     * Retrieves information about a specific partition for an Event Hub, including elements that describe the available
     * events in the partition event stream.
     *
     * @param partitionId The unique identifier of a partition associated with the Event Hub.
     * @return The set of information for the requested partition under the Event Hub this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * @return A new {@link EventHubAsyncProducer}.
     */
    public EventHubAsyncProducer createProducer() {
        return createProducer(defaultProducerOptions);
    }

    /**
     * Creates an Event Hub producer responsible for transmitting {@link EventData} to the Event Hub, grouped together
     * in batches. If {@link EventHubProducerOptions#getPartitionId() options.partitionId()} is not {@code null}, the
     * events are routed to that specific partition. Otherwise, events are automatically routed to an available
     * partition.
     *
     * @param options The set of options to apply when creating the producer.
     * @return A new {@link EventHubAsyncProducer}.
     * @throws NullPointerException if {@code options} is {@code null}.
     */
    public EventHubAsyncProducer createProducer(EventHubProducerOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        final EventHubProducerOptions clonedOptions = options.clone();

        if (clonedOptions.getRetry() == null) {
            clonedOptions.setRetry(connectionOptions.getRetry());
        }

        final String entityPath;
        final String linkName;

        if (ImplUtils.isNullOrEmpty(options.getPartitionId())) {
            entityPath = eventHubName;
            linkName = StringUtil.getRandomString("EC");
        } else {
            entityPath = String.format(Locale.US, SENDER_ENTITY_PATH_FORMAT, eventHubName, options.getPartitionId());
            linkName = StringUtil.getRandomString("PS");
        }

        final Mono<AmqpSendLink> amqpLinkMono = connectionMono
            .flatMap(connection -> connection.createSession(entityPath))
            .flatMap(session -> {
                logger.verbose("Creating producer for {}", entityPath);
                final RetryPolicy retryPolicy = RetryUtil.getRetryPolicy(clonedOptions.getRetry());

                return session.createProducer(linkName, entityPath, clonedOptions.getRetry().getTryTimeout(),
                    retryPolicy).cast(AmqpSendLink.class);
            });

        return new EventHubAsyncProducer(amqpLinkMono, clonedOptions, tracerProvider, messageSerializer);
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
     * #DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     * @param partitionId The identifier of the Event Hub partition.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @return A new {@link EventHubAsyncConsumer} that receives events from the partition at the given position.
     * @throws NullPointerException If {@code eventPosition}, or {@code options} is {@code null}.
     * @throws IllegalArgumentException If {@code consumerGroup} or {@code partitionId} is {@code null} or an empty
     * string.
     */
    public EventHubAsyncConsumer createConsumer(String consumerGroup, String partitionId, EventPosition eventPosition) {
        return createConsumer(consumerGroup, partitionId, eventPosition, defaultConsumerOptions);
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
     * context of this group. The name of the consumer group that is created by default is {@link
     * #DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     * @param partitionId The identifier of the Event Hub partition from which events will be received.
     * @param eventPosition The position within the partition where the consumer should begin reading events.
     * @param options The set of options to apply when creating the consumer.
     * @return An new {@link EventHubAsyncConsumer} that receives events from the partition with all configured {@link
     * EventHubConsumerOptions}.
     * @throws NullPointerException If {@code eventPosition}, {@code consumerGroup}, {@code partitionId}, or
     * {@code options} is {@code null}.
     * @throws IllegalArgumentException If {@code consumerGroup} or {@code partitionId} is an empty string.
     */
    public EventHubAsyncConsumer createConsumer(String consumerGroup, String partitionId, EventPosition eventPosition,
        EventHubConsumerOptions options) {
        Objects.requireNonNull(eventPosition, "'eventPosition' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");
        Objects.requireNonNull(consumerGroup, "'consumerGroup' cannot be null.");
        Objects.requireNonNull(partitionId, "'partitionId' cannot be null.");

        if (consumerGroup.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'consumerGroup' cannot be an empty string."));
        } else if (partitionId.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'partitionId' cannot be an empty string."));
        }

        final EventHubConsumerOptions clonedOptions = options.clone();
        if (clonedOptions.getScheduler() == null) {
            clonedOptions.setScheduler(connectionOptions.getScheduler());
        }
        if (clonedOptions.getRetry() == null) {
            clonedOptions.setRetry(connectionOptions.getRetry());
        }

        final String linkName = StringUtil.getRandomString("PR");
        final String entityPath =
            String.format(Locale.US, RECEIVER_ENTITY_PATH_FORMAT, eventHubName, consumerGroup, partitionId);

        final Mono<AmqpReceiveLink> receiveLinkMono = connectionMono.flatMap(connection ->
            connection.createSession(entityPath).cast(EventHubSession.class)).flatMap(session -> {
                logger.verbose("Creating consumer for path: {}", entityPath);
                final RetryPolicy retryPolicy = RetryUtil.getRetryPolicy(clonedOptions.getRetry());

                return session.createConsumer(linkName, entityPath, clonedOptions.getRetry().getTryTimeout(),
                    retryPolicy, eventPosition, options);
            });

        return new EventHubAsyncConsumer(receiveLinkMono, messageSerializer, clonedOptions);
    }

    /**
     * Closes and disposes of connection to service. Any {@link EventHubAsyncConsumer EventHubConsumers} and {@link
     * EventHubAsyncProducer EventHubProducers} created with this instance will have their connections closed.
     */
    @Override
    public void close() {
        if (hasConnection.getAndSet(false)) {
            try {
                final AmqpConnection connection = connectionMono.block(connectionOptions.getRetry().getTryTimeout());
                if (connection != null) {
                    connection.close();
                }
            } catch (IOException exception) {
                throw logger.logExceptionAsError(
                    new AmqpException(false, "Unable to close connection to service", exception,
                        new ErrorContext(connectionOptions.getHostname())));
            }
        }
    }
}
