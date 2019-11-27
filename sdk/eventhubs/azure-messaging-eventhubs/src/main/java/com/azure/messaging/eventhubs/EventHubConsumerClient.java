// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.SynchronousEventSubscriber;
import com.azure.messaging.eventhubs.implementation.SynchronousReceiveWork;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.Closeable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A <b>synchronous</b> consumer responsible for reading {@link EventData} from an Event Hub partition in the context of
 * a specific consumer group.
 *
 * <p><strong>Creating a synchronous consumer</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerclient.instantiation}
 *
 * <p><strong>Consuming events from a single partition</strong></p>
 * <p>Events from a single partition can be consumed using {@link #receiveFromPartition(String, int, EventPosition)} or
 * {@link #receiveFromPartition(String, int, EventPosition, Duration)}. The call to `receive` completes and returns an
 * {@link IterableStream} when either the maximum number of events is received, or the timeout has elapsed.</p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerclient.receive#string-int-eventposition-duration}
 */
@ServiceClient(builder = EventHubClientBuilder.class)
public class EventHubConsumerClient implements Closeable {
    private final ClientLogger logger = new ClientLogger(EventHubConsumerClient.class);

    private final EventHubConsumerAsyncClient consumer;
    private final ReceiveOptions defaultReceiveOptions = new ReceiveOptions();
    private final Duration timeout;
    private final AtomicInteger idGenerator = new AtomicInteger();

    EventHubConsumerClient(EventHubConsumerAsyncClient consumer, Duration tryTimeout) {
        Objects.requireNonNull(tryTimeout, "'tryTimeout' cannot be null.");

        this.consumer = Objects.requireNonNull(consumer, "'consumer' cannot be null.");
        this.timeout = tryTimeout;
    }

    /**
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with.
     */
    public String getFullyQualifiedNamespace() {
        return consumer.getFullyQualifiedNamespace();
    }

    /**
     * Gets the Event Hub name this client interacts with.
     *
     * @return The Event Hub name this client interacts with.
     */
    public String getEventHubName() {
        return consumer.getEventHubName();
    }

    /**
     * Gets the consumer group this consumer is reading events as a part of.
     *
     * @return The consumer group this consumer is reading events as a part of.
     */
    public String getConsumerGroup() {
        return consumer.getConsumerGroup();
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EventHubProperties getEventHubProperties() {
        return consumer.getEventHubProperties().block(timeout);
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return The set of identifiers for the partitions of an Event Hub.
     */
    public IterableStream<String> getPartitionIds() {
        return new IterableStream<>(consumer.getPartitionIds());
    }

    /**
     * Retrieves information about a specific partition for an Event Hub, including elements that describe the available
     * events in the partition event stream.
     *
     * @param partitionId The unique identifier of a partition associated with the Event Hub.
     *
     * @return The set of information for the requested partition under the Event Hub this client is associated with.
     *
     * @throws NullPointerException if {@code partitionId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PartitionProperties getPartitionProperties(String partitionId) {
        return consumer.getPartitionProperties(partitionId).block(timeout);
    }

    /**
     * Receives a batch of {@link PartitionEvent events} from the Event Hub partition.
     *
     * @param maximumMessageCount The maximum number of messages to receive in this batch.
     * @param partitionId Identifier of the partition to read events from.
     * @param startingPosition Position within the Event Hub partition to begin consuming events.
     *
     * @return A set of {@link PartitionEvent} that was received. The iterable contains up to
     *     {@code maximumMessageCount} events. If a stream for the events was opened before, the same position within
     *     that partition is returned. Otherwise, events are read starting from {@code startingPosition}.
     *
     * @throws NullPointerException if {@code partitionId}, or {@code startingPosition} is null.
     * @throws IllegalArgumentException if {@code maximumMessageCount} is less than 1, or if {@code partitionId} is an
     *     empty string.
     */
    public IterableStream<PartitionEvent> receiveFromPartition(String partitionId, int maximumMessageCount,
        EventPosition startingPosition) {
        return receiveFromPartition(partitionId, maximumMessageCount, startingPosition, timeout);
    }

    /**
     * Receives a batch of {@link PartitionEvent events} from the Event Hub partition.
     *
     * @param partitionId Identifier of the partition to read events from.
     * @param maximumMessageCount The maximum number of messages to receive in this batch.
     * @param startingPosition Position within the Event Hub partition to begin consuming events.
     * @param maximumWaitTime The maximum amount of time to wait to build up the requested message count for the
     *     batch; if not specified, the default wait time specified when the consumer was created will be used.
     *
     * @return A set of {@link PartitionEvent} that was received. The iterable contains up to
     *     {@code maximumMessageCount} events.
     *
     * @throws NullPointerException if {@code partitionId}, {@code maximumWaitTime}, or {@code startingPosition} is
     *     {@code null}.
     * @throws IllegalArgumentException if {@code maximumMessageCount} is less than 1 or {@code maximumWaitTime} is
     *     zero or a negative duration.
     */
    public IterableStream<PartitionEvent> receiveFromPartition(String partitionId, int maximumMessageCount,
        EventPosition startingPosition, Duration maximumWaitTime) {
        if (Objects.isNull(maximumWaitTime)) {
            throw logger.logExceptionAsError(new NullPointerException("'maximumWaitTime' cannot be null."));
        } else if (Objects.isNull(startingPosition)) {
            throw logger.logExceptionAsError(new NullPointerException("'startingPosition' cannot be null."));
        } else if (Objects.isNull(partitionId)) {
            throw logger.logExceptionAsError(new NullPointerException("'partitionId' cannot be null."));
        }

        if (partitionId.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'partitionId' cannot be empty."));
        }
        if (maximumMessageCount < 1) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maximumMessageCount' cannot be less than 1."));
        } else if (maximumWaitTime.isNegative() || maximumWaitTime.isZero()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maximumWaitTime' cannot be zero or less."));
        }

        final Flux<PartitionEvent> events = Flux.create(emitter -> {
            queueWork(partitionId, maximumMessageCount, startingPosition, maximumWaitTime, defaultReceiveOptions,
                emitter);
        });

        return new IterableStream<>(events);
    }

    /**
     * Receives a batch of {@link PartitionEvent events} from the Event Hub partition.
     *
     * @param partitionId Identifier of the partition to read events from.
     * @param maximumMessageCount The maximum number of messages to receive in this batch.
     * @param startingPosition Position within the Event Hub partition to begin consuming events.
     * @param maximumWaitTime The maximum amount of time to wait to build up the requested message count for the
     *     batch; if not specified, the default wait time specified when the consumer was created will be used.
     * @param receiveOptions Options when receiving events from the partition.

     * @return A set of {@link PartitionEvent} that was received. The iterable contains up to
     *     {@code maximumMessageCount} events.
     *
     * @throws NullPointerException if {@code maximumWaitTime}, {@code startingPosition}, {@code partitionId}, or
     *     {@code receiveOptions} is {@code null}.
     * @throws IllegalArgumentException if {@code maximumMessageCount} is less than 1 or {@code maximumWaitTime} is
     *     zero or a negative duration.
     */
    public IterableStream<PartitionEvent> receiveFromPartition(String partitionId, int maximumMessageCount,
        EventPosition startingPosition, Duration maximumWaitTime, ReceiveOptions receiveOptions) {
        if (Objects.isNull(maximumWaitTime)) {
            throw logger.logExceptionAsError(new NullPointerException("'maximumWaitTime' cannot be null."));
        } else if (Objects.isNull(startingPosition)) {
            throw logger.logExceptionAsError(new NullPointerException("'startingPosition' cannot be null."));
        } else if (Objects.isNull(partitionId)) {
            throw logger.logExceptionAsError(new NullPointerException("'partitionId' cannot be null."));
        } else if (Objects.isNull(receiveOptions)) {
            throw logger.logExceptionAsError(new NullPointerException("'receiveOptions' cannot be null."));
        }

        if (partitionId.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'partitionId' cannot be empty."));
        }
        if (maximumMessageCount < 1) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maximumMessageCount' cannot be less than 1."));
        } else if (maximumWaitTime.isNegative() || maximumWaitTime.isZero()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maximumWaitTime' cannot be zero or less."));
        }

        final Flux<PartitionEvent> events = Flux.create(emitter -> {
            queueWork(partitionId, maximumMessageCount, startingPosition, maximumWaitTime, receiveOptions, emitter);
        });

        return new IterableStream<>(events);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        consumer.close();
    }

    /**
     * Given an {@code emitter}, queues that work in {@link SynchronousEventSubscriber}. If the synchronous job has not
     * been created, will initialise it.
     */
    private void queueWork(String partitionId, int maximumMessageCount, EventPosition startingPosition,
        Duration maximumWaitTime, ReceiveOptions receiveOptions, FluxSink<PartitionEvent> emitter) {
        final long id = idGenerator.getAndIncrement();
        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, maximumMessageCount, maximumWaitTime,
            emitter);
        final SynchronousEventSubscriber syncSubscriber = new SynchronousEventSubscriber(work);
        logger.info("Started synchronous event subscriber for partition '{}'.", partitionId);
        consumer.receiveFromPartition(partitionId, startingPosition, receiveOptions).subscribeWith(syncSubscriber);
    }
}
