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
import com.azure.messaging.eventhubs.models.PartitionEvent;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
 * <p><strong>Consuming events from Event Hub</strong></p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive}
 *
 * <p><strong>Rate limiting consumption of events from Event Hub</strong></p>
 *
 * For event consumers that need to limit the number of events they receive at a given time, they can use {@link
 * BaseSubscriber#request(long)}.
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#basesubscriber}
 */
@Immutable
public class EventHubConsumerAsyncClient implements Closeable {
    private static final String RECEIVER_ENTITY_PATH_FORMAT = "%s/ConsumerGroups/%s/Partitions/%s";

    private final ConcurrentHashMap<String, EventHubPartitionAsyncConsumer> openPartitionConsumers =
        new ConcurrentHashMap<>();

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(EventHubConsumerAsyncClient.class);
    private final String fullyQualifiedNamespace;
    private final String eventHubName;
    private final EventHubLinkProvider linkProvider;
    private final MessageSerializer messageSerializer;
    private final String consumerGroup;
    private final EventPosition startingPosition;
    private final EventHubConsumerOptions consumerOptions;
    private final Flux<PartitionEvent> allPartitionsFlux;

    EventHubConsumerAsyncClient(String fullyQualifiedNamespace, String eventHubName, EventHubLinkProvider linkProvider,
        MessageSerializer messageSerializer, String consumerGroup, EventPosition startingPosition,
        EventHubConsumerOptions consumerOptions) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.eventHubName = eventHubName;
        this.linkProvider = linkProvider;
        this.messageSerializer = messageSerializer;
        this.consumerGroup = consumerGroup;
        this.startingPosition = startingPosition;
        this.consumerOptions = consumerOptions;
        this.allPartitionsFlux = Flux.defer(() -> {
            return getPartitionIds().map(id -> {
                final EventHubPartitionAsyncConsumer partitionConsumer = createPartitionConsumer(id);
                logger.info("Creating receive-all-consumer for partition '{}'", id);
                openPartitionConsumers.put("receive-all-" + id, partitionConsumer);
                return partitionConsumer;
            }).flatMap(consumer -> consumer.receive());
        }).share();
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
     * Gets the position of the event in the partition where the consumer should begin reading.
     *
     * @return The position of the event in the partition where the consumer should begin reading.
     */
    public EventPosition getStartingPosition() {
        return startingPosition;
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
        return linkProvider.getManagementNode().flatMap(EventHubManagementNode::getEventHubProperties);
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
        return linkProvider.getManagementNode().flatMap(node -> node.getPartitionProperties(partitionId));
    }

    /**
     * Begin consuming events from a single partition starting at {@link #getStartingPosition()} until there are no more
     * subscribers.
     *
     * @param partitionId Identifier of the partition to read events from.
     * @return A stream of events for this partition. If a stream for the events was opened before, the same position
     *     within that partition is returned. Otherwise, events are read starting from {@link #getStartingPosition()}.
     *
     * @throws NullPointerException if {@code partitionId} is null.
     * @throws IllegalArgumentException if {@code partitionId} is an empty string.
     */
    public Flux<PartitionEvent> receive(String partitionId) {
        if (partitionId == null) {
            return Flux.error(logger.logExceptionAsError(new NullPointerException("'partitionId' cannot be null.")));
        } else if (partitionId.isEmpty()) {
            return Flux.error(logger.logExceptionAsError(
                new IllegalArgumentException("'partitionId' cannot be an empty string.")));
        }

        return openPartitionConsumers.computeIfAbsent(partitionId, id -> createPartitionConsumer(id)).receive();
    }

    /**
     * Begin consuming events from all partitions starting at {@link #getStartingPosition()} until there are no more
     * subscribers.
     *
     * @return A stream of events for every partition in the Event Hub. Otherwise, events are read starting from
     *     {@link #getStartingPosition()}.
     *
     * @throws NullPointerException if {@code partitionId} is null.
     * @throws IllegalArgumentException if {@code partitionId} is an empty string.
     */
    public Flux<PartitionEvent> receive() {
        return allPartitionsFlux;
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     *
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

            //TODO (conniey): Depending on whether or not shared connection, dispose of connection.
        }
    }

    private EventHubPartitionAsyncConsumer createPartitionConsumer(String partitionId) {
        final String linkName = StringUtil.getRandomString("PR");
        final String entityPath = String.format(Locale.US, RECEIVER_ENTITY_PATH_FORMAT,
            getEventHubName(), consumerGroup, partitionId);

        final Mono<AmqpReceiveLink> receiveLinkMono =
            linkProvider.createReceiveLink(linkName, entityPath, getStartingPosition(), consumerOptions)
                .doOnNext(next -> logger.verbose("Creating consumer for path: {}", next.getEntityPath()));

        return new EventHubPartitionAsyncConsumer(receiveLinkMono, messageSerializer,
            getEventHubName(), consumerGroup, partitionId, consumerOptions);
    }
}
