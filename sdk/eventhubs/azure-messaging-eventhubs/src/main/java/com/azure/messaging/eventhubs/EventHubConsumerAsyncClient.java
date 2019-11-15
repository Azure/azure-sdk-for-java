// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.annotation.Immutable;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A consumer responsible for reading {@link EventData} from a specific Event Hub partition in the context of a specific
 * consumer group.
 *
 * <ul>
 * <li>If {@link EventHubConsumerAsyncClient} is created where {@link EventHubConsumerOptions#getOwnerLevel()} has a
 * value, then Event Hubs service will guarantee only one active consumer exists per partitionId and consumer group
 * combination. This consumer is sometimes referred to as an "Epoch Consumer."</li>
 * <li>Multiple consumers per partitionId and consumer group combination can be created by not setting
 * {@link EventHubConsumerOptions#getOwnerLevel()} when creating consumers. This non-exclusive consumer is sometimes
 * referred to as a "Non-Epoch Consumer."</li>
 * </ul>
 *
 * <p><strong>Creating an {@link EventHubConsumerAsyncClient}</strong></p>
 * <p>Required parameters are {@code consumerGroup}, {@code startingPosition}, and credentials are required when
 * creating a consumer.</p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.instantiation}
 *
 * <p><strong>Consuming events a single partition from Event Hub</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string}
 *
 * <p><strong>Rate limiting consumption of events from Event Hub</strong></p>
 * <p>For event consumers that need to limit the number of events they receive at a given time, they can use {@link
 * BaseSubscriber#request(long)}.</p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-basesubscriber}
 *
 * <p><strong>Receiving from all partitions</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive}
 *
 * <p><strong>Viewing latest partition information</strong></p>
 * <p>Latest partition information as events are received can by setting
 * {@link EventHubConsumerOptions#setTrackLastEnqueuedEventProperties(boolean) setTrackLastEnqueuedEventProperties} to
 * {@code true}. As events come in, explore the {@link PartitionContext} object.
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#lastenqueuedeventproperties}
 */
@Immutable
public class EventHubConsumerAsyncClient implements Closeable {
    private static final String RECEIVER_ENTITY_PATH_FORMAT = "%s/ConsumerGroups/%s/Partitions/%s";

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(EventHubConsumerAsyncClient.class);
    private final String fullyQualifiedNamespace;
    private final String eventHubName;
    private final EventHubConnection connection;
    private final MessageSerializer messageSerializer;
    private final String consumerGroup;
    private final EventHubConsumerOptions consumerOptions;
    private final boolean isSharedConnection;
    private final ConcurrentHashMap<String, EventHubPartitionAsyncConsumer> openPartitionConsumers =
        new ConcurrentHashMap<>();
    private final AtomicReference<EventPosition> receiveAllStartingPosition = new AtomicReference<>();

    private Flux<PartitionEvent> receiveAllFlux;

    EventHubConsumerAsyncClient(String fullyQualifiedNamespace, String eventHubName, EventHubConnection connection,
        MessageSerializer messageSerializer, String consumerGroup, EventHubConsumerOptions consumerOptions,
        boolean isSharedConnection) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.eventHubName = eventHubName;
        this.connection = connection;
        this.messageSerializer = messageSerializer;
        this.consumerGroup = consumerGroup;
        this.consumerOptions = consumerOptions;
        this.isSharedConnection = isSharedConnection;
    }

    /**
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
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
     * Gets the consumer group this consumer is reading events as a part of.
     *
     * @return The consumer group this consumer is reading events as a part of.
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EventHubProperties> getProperties() {
        return connection.getManagementNode().flatMap(EventHubManagementNode::getEventHubProperties);
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
     *
     * @return The set of information for the requested partition under the Event Hub this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PartitionProperties> getPartitionProperties(String partitionId) {
        if (Objects.isNull(partitionId)) {
            return monoError(logger, new NullPointerException("'partitionId' cannot be null."));
        } else if (partitionId.isEmpty()) {
            return monoError(logger, new IllegalArgumentException("'partitionId' cannot be an empty string."));
        }

        return connection.getManagementNode().flatMap(node -> node.getPartitionProperties(partitionId));
    }

    /**
     * Begin consuming events from a single partition starting at {@code startingPosition}.
     *
     * @param partitionId Identifier of the partition to read events from.
     * @param startingPosition Position within the Event Hub partition to begin consuming events.
     *
     * @return A stream of events for this partition. If a stream for the events was opened before, the same position
     *     within that partition is returned. Otherwise, events are read starting from {@code startingPosition}.
     *
     * @throws NullPointerException if {@code partitionId} or {@code startingPosition} is null.
     * @throws IllegalArgumentException if {@code partitionId} is an empty string. If there is an existing consumer
     *     for {@code partitionId} but it does not have the same {@code startingPosition}.
     */
    public Flux<PartitionEvent> receive(String partitionId, EventPosition startingPosition) {
        if (Objects.isNull(partitionId)) {
            return Flux.error(logger.logExceptionAsError(new NullPointerException("'partitionId' cannot be null.")));
        } else if (partitionId.isEmpty()) {
            return Flux.error(logger.logExceptionAsError(
                new IllegalArgumentException("'partitionId' cannot be an empty string.")));
        }

        if (Objects.isNull(startingPosition)) {
            return Flux.error((logger.logExceptionAsError(
                new NullPointerException("'startingPosition' cannot be null."))));
        }

        final EventHubPartitionAsyncConsumer consumer = openPartitionConsumers
            .computeIfAbsent(partitionId, id -> createPartitionConsumer(id, startingPosition));

        if (!consumer.startingPosition().equals(startingPosition)) {
            return Flux.error(logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.ROOT,
                "Consumer for partition '%s' exists already but does not have the same starting position. "
                    + "Existing: %s, Requested: %s", partitionId, consumer.startingPosition(), startingPosition))));
        }

        return consumer.receive();
    }

    /**
     * Begin consuming events from all partitions starting at {@code startingPosition}.
     *
     * @param startingPosition Position within each Event Hub partition to begin consuming events.
     *
     * @return A stream of events for every partition in the Event Hub. Otherwise, events are read starting from {@link
     *     EventPosition#earliest()}.
     *
     * @throws NullPointerException if {@code startingPosition} is null.
     * @throws IllegalArgumentException If there is an existing consumer for all partitions but does not have the
     *     same {@code startingPosition}.
     */
    public Flux<PartitionEvent> receive(EventPosition startingPosition) {
        if (Objects.isNull(startingPosition)) {
            return Flux.error((logger.logExceptionAsError(
                new NullPointerException("'startingPosition' cannot be null."))));
        }

        // This is successful when we have not opened a receive for all partitions, yet.
        if (receiveAllStartingPosition.compareAndSet(null, startingPosition)) {
            final Flux<PartitionEvent> allPartitionEvents = getPartitionIds().map(partitionId -> {
                final String linkName = "receive-all-" + partitionId;
                logger.info("Creating receive consumer for partition '{}'", partitionId);
                return openPartitionConsumers.computeIfAbsent(linkName,
                    existingKey -> createPartitionConsumer(existingKey, startingPosition));

            }).flatMap(consumer -> consumer.receive());

            receiveAllFlux = Flux.merge(allPartitionEvents);
            return receiveAllFlux;
        }

        // See if the existing receive all starting position is the same as the one requested.
        final EventPosition existingStartingPosition = receiveAllStartingPosition.get();
        return existingStartingPosition.equals(startingPosition)
            ? receiveAllFlux
            : Flux.error(logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.ROOT,
            "Consumer for all partitions exists already but does not have the same starting position. "
                + "Existing: %s, Requested: %s", existingStartingPosition, startingPosition))));
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (!isDisposed.getAndSet(true)) {
            openPartitionConsumers.forEach((key, value) -> {
                try {
                    value.close();
                } catch (IOException e) {
                    logger.warning("Exception occurred while closing consumer for partition '{}'", key, e);
                }
            });
            openPartitionConsumers.clear();

            if (!isSharedConnection) {
                connection.close();
            }
        }
    }

    private EventHubPartitionAsyncConsumer createPartitionConsumer(String partitionId, EventPosition startingPosition) {
        final String linkName = StringUtil.getRandomString("PR");
        final String entityPath = String.format(Locale.US, RECEIVER_ENTITY_PATH_FORMAT,
            getEventHubName(), consumerGroup, partitionId);

        final Mono<AmqpReceiveLink> receiveLinkMono =
            connection.createReceiveLink(linkName, entityPath, startingPosition, consumerOptions)
                .doOnNext(next -> logger.verbose("Creating consumer for path: {}", next.getEntityPath()));

        return new EventHubPartitionAsyncConsumer(receiveLinkMono, messageSerializer,
            getEventHubName(), consumerGroup, partitionId, startingPosition, consumerOptions);
    }
}
