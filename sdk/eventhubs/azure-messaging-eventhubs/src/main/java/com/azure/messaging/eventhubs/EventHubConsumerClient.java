// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.SynchronousEventSubscriber;
import com.azure.messaging.eventhubs.implementation.SynchronousReceiveWork;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.Closeable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A consumer responsible for reading {@link EventData} from a specific Event Hub partition in the context of a specific
 * consumer group.
 *
 * <ul>
 * <li>If {@link EventHubConsumerClient} is created where {@link EventHubConsumerOptions#getOwnerLevel()} has a
 * value, then Event Hubs service will guarantee only one active consumer exists per partitionId and consumer group
 * combination. This consumer is sometimes referred to as an "Epoch Consumer."</li>
 * <li>Multiple consumers per partitionId and consumer group combination can be created by not setting
 * {@link EventHubConsumerOptions#getOwnerLevel()} when creating consumers. This non-exclusive consumer is sometimes
 * referred to as a "Non-Epoch Consumer."</li>
 * </ul>
 *
 * <p><strong>Creating a synchronous consumer</strong></p>
 * Create an {@link EventHubConsumerClient} using {@link EventHubClientBuilder}.
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerclient.instantiation}
 *
 * <p><strong>Consuming events from a single partition</strong></p>
 * Events from a single partition can be consumed using {@link #receive(String, int, EventPosition)} or
 * {@link #receive(String, int, EventPosition, Duration)}. The call to `receive` completes and returns an
 * {@link IterableStream} when either the maximum number of events is reached, or the timeout duration is reached.
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerclient.receive#string-int-eventposition-duration}
 */
public class EventHubConsumerClient implements Closeable {
    private static final String RECEIVE_ALL_KEY = "receive-all";

    private final ClientLogger logger = new ClientLogger(EventHubConsumerClient.class);
    private final ConcurrentHashMap<String, SynchronousEventSubscriber> openSubscribers = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    private final EventHubConsumerAsyncClient consumer;
    private final Duration timeout;

    EventHubConsumerClient(EventHubConsumerAsyncClient consumer, Duration tryTimeout) {
        Objects.requireNonNull(tryTimeout, "'tryTimeout' cannot be null.");

        this.consumer = Objects.requireNonNull(consumer, "'consumer' cannot be null.");
        this.timeout = tryTimeout;
    }

    /**
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with
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
    public EventHubProperties getProperties() {
        return consumer.getProperties().block(timeout);
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return A Flux of identifiers for the partitions of an Event Hub.
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
     */
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
     * @throws IllegalArgumentException if {@code maximumMessageCount} is less than 1.
     */
    public IterableStream<PartitionEvent> receive(String partitionId, int maximumMessageCount,
        EventPosition startingPosition) {
        return receive(partitionId, maximumMessageCount, startingPosition, timeout);
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
     * @throws NullPointerException if {@code maximumWaitTime} or {@code eventPosition} is null.
     * @throws IllegalArgumentException if {@code maximumMessageCount} is less than 1 or {@code maximumWaitTime} is
     *     zero or a negative duration.
     */
    public IterableStream<PartitionEvent> receive(String partitionId, int maximumMessageCount,
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
            queueWork(partitionId, maximumMessageCount, startingPosition, maximumWaitTime, emitter);
        });

        return new IterableStream<>(events);
    }

    /**
     * Receives a batch of {@link PartitionEvent events} from all partitions. The batch is returned after
     * {@code maximumMessageCount} events are received or after the
     * {@link RetryOptions#getTryTimeout() operation timeout} has elapsed.
     *
     * @param maximumMessageCount The maximum number of messages to receive in this batch.
     *
     * @return A set of {@link PartitionEvent} that was received. The iterable contains up to
     *     {@code maximumMessageCount} events.
     *
     * @throws IllegalArgumentException if {@code maximumMessageCount} is less than 1.
     */
    public IterableStream<PartitionEvent> receive(int maximumMessageCount, EventPosition startingPosition) {
        return receive(maximumMessageCount, startingPosition, timeout);
    }

    /**
     * Receives a batch of {@link PartitionEvent events} from all partitions. The batch is returned after
     * {@code maximumMessageCount} events are received or after {@code maximumWaitTime} has elapsed.
     *
     * @param maximumMessageCount The maximum number of messages to receive in this batch.
     * @param maximumWaitTime The maximum amount of time to wait to build up the requested message count for the
     *     batch; if not specified, the default wait time specified when the consumer was created will be used.
     *
     * @return A set of {@link PartitionEvent} that was received. The iterable contains up to
     *     {@code maximumMessageCount} events.
     *
     * @throws NullPointerException if {@code maximumWaitTime} is null.
     * @throws IllegalArgumentException if {@code maximumMessageCount} is less than 1 or {@code maximumWaitTime} is
     *     zero or a negative duration.
     */
    public IterableStream<PartitionEvent> receive(int maximumMessageCount, EventPosition startingPosition,
        Duration maximumWaitTime) {
        Objects.requireNonNull(maximumWaitTime, "'maximumWaitTime' cannot be null.");

        if (maximumMessageCount < 1) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maximumMessageCount' cannot be less than 1."));
        } else if (maximumWaitTime.isNegative() || maximumWaitTime.isZero()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maximumWaitTime' cannot be zero or less."));
        }

        final Flux<PartitionEvent> events = Flux.create(emitter -> {
            queueWork(RECEIVE_ALL_KEY, maximumMessageCount, startingPosition, maximumWaitTime, emitter);
        });

        return new IterableStream<>(events);
    }

    /**
     * Given an {@code emitter}, queues that work in {@link SynchronousEventSubscriber}. If the synchronous job has not
     * been created, will initialise it.
     */
    private void queueWork(String partitionId, int maximumMessageCount, EventPosition startingPosition,
        Duration maximumWaitTime, FluxSink<PartitionEvent> emitter) {
        final long id = idGenerator.getAndIncrement();
        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, maximumMessageCount, maximumWaitTime,
            emitter);

        final SynchronousEventSubscriber subscriber = openSubscribers.computeIfAbsent(partitionId, key -> {
            SynchronousEventSubscriber syncSubscriber = new SynchronousEventSubscriber();

            if (RECEIVE_ALL_KEY.equals(key)) {
                logger.info("Started synchronous event subscriber for all partitions");
                consumer.receive(startingPosition).subscribeWith(syncSubscriber);
            } else {
                logger.info("Started synchronous event subscriber for partition '{}'.", key);
                consumer.receive(key, startingPosition).subscribeWith(syncSubscriber);
            }
            return syncSubscriber;
        });

        logger.info("Queueing work item in SynchronousEventSubscriber.");
        subscriber.queueReceiveWork(work);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        consumer.close();
    }
}
