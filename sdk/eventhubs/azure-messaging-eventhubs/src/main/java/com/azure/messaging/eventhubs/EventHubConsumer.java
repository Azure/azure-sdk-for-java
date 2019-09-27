// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.SynchronousEventSubscriber;
import com.azure.messaging.eventhubs.implementation.SynchronousReceiveWork;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A consumer responsible for reading {@link EventData} from a specific Event Hub partition in the context of a specific
 * consumer group.
 *
 * <ul>
 * <li>If {@link EventHubConsumer} is created where {@link EventHubConsumerOptions#getOwnerLevel()} has a
 * value, then Event Hubs service will guarantee only one active consumer exists per partitionId and consumer group
 * combination. This consumer is sometimes referred to as an "Epoch Consumer."</li>
 * <li>Multiple consumers per partitionId and consumer group combination can be created by not setting
 * {@link EventHubConsumerOptions#getOwnerLevel()} when creating consumers. This non-exclusive consumer is sometimes
 * referred to as a "Non-Epoch Consumer."</li>
 * </ul>
 *
 * <p><strong>Creating a synchronous consumer</strong></p>
 * Create an {@link EventHubConsumer} using {@link EventHubClient}.
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumer.instantiation}
 *
 * <p><strong>Consuming events from an Event Hub</strong></p>
 * Events can be consumed using {@link #receive(int)} or {@link #receive(int, Duration)}. The call to `receive`
 * completes and returns an {@link IterableStream} when either the number of events is reached, or the
 * timeout duration is reached.
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumer.receive#int-duration}
 *
 * @see EventHubClient#createConsumer(String, String, EventPosition)
 * @see EventHubClient#createConsumer(String, String, EventPosition, EventHubConsumerOptions)
 */
public class EventHubConsumer implements Closeable {
    private static final AtomicReferenceFieldUpdater<EventHubConsumer, SynchronousEventSubscriber> SUBSCRIBER =
        AtomicReferenceFieldUpdater.newUpdater(EventHubConsumer.class, SynchronousEventSubscriber.class,
            "eventSubscriber");

    private final ClientLogger logger = new ClientLogger(EventHubConsumer.class);
    private final AtomicLong idGenerator = new AtomicLong();

    private final EventHubAsyncConsumer consumer;
    private final Duration timeout;
    private volatile SynchronousEventSubscriber eventSubscriber;

    EventHubConsumer(EventHubAsyncConsumer consumer, Duration tryTimeout) {
        Objects.requireNonNull(tryTimeout, "'tryTimeout' cannot be null.");

        this.consumer = Objects.requireNonNull(consumer, "'consumer' cannot be null.");
        this.timeout = tryTimeout;
    }

    /**
     * Receives a batch of EventData from the Event Hub partition.
     *
     * @param maximumMessageCount The maximum number of messages to receive in this batch.
     * @return A set of {@link EventData} that was received. The iterable contains up to {@code maximumMessageCount}
     *     events.
     * @throws IllegalArgumentException if {@code maximumMessageCount} is less than 1.
     */
    public IterableStream<EventData> receive(int maximumMessageCount) {
        return receive(maximumMessageCount, timeout);
    }

    /**
     * Receives a batch of EventData from the Event Hub partition
     *
     * @param maximumMessageCount The maximum number of messages to receive in this batch.
     * @param maximumWaitTime The maximum amount of time to wait to build up the requested message count for the
     *     batch; if not specified, the default wait time specified when the consumer was created will be used.
     * @return A set of {@link EventData} that was received. The iterable contains up to {@code maximumMessageCount}
     *     events.
     * @throws NullPointerException if {@code maximumWaitTime} is null.
     * @throws IllegalArgumentException if {@code maximumMessageCount} is less than 1 or {@code maximumWaitTime} is
     *     zero or a negative duration.
     */
    public IterableStream<EventData> receive(int maximumMessageCount, Duration maximumWaitTime) {
        Objects.requireNonNull(maximumWaitTime, "'maximumWaitTime' cannot be null.");

        if (maximumMessageCount < 1) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maximumMessageCount' cannot be less than 1."));
        } else if (maximumWaitTime.isNegative() || maximumWaitTime.isZero()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'maximumWaitTime' cannot be zero or less."));
        }

        final Flux<EventData> events = Flux.create(emitter -> {
            queueWork(maximumMessageCount, maximumWaitTime, emitter);
        });

        final Flux<EventData> map = events.collectList().map(x -> {
            logger.info("Number of events received: {}", x.size());
            return Flux.fromIterable(x);
        }).block();
        return new IterableStream<>(map);
    }

    /**
     * A set of information about the last enqueued event of a partition, as observed by the consumer as events are
     * received from the Event Hubs service.
     *
     * @return {@code null} if {@link EventHubConsumerOptions#getLastEnqueuedEventProperties()} was not set when
     *     creating the consumer. Otherwise, the properties describing the most recently enqueued event in the
     *     partition.
     */
    public LastEnqueuedEventProperties getLastEnqueuedEventProperties() {
        return consumer.getLastEnqueuedEventProperties();
    }

    /**
     * Given an {@code emitter}, queues that work in {@link SynchronousEventSubscriber}. If the {@link #eventSubscriber}
     * has not been initialised yet, will initialise it.
     */
    private void queueWork(int maximumMessageCount, Duration maximumWaitTime, FluxSink<EventData> emitter) {
        final long id = idGenerator.getAndIncrement();
        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, maximumMessageCount, maximumWaitTime,
            emitter);

        if (SUBSCRIBER.compareAndSet(this, null, new SynchronousEventSubscriber(work))) {
            logger.info("Started synchronous event subscriber.");
            consumer.receive().subscribeWith(SUBSCRIBER.get(this));
        } else {
            logger.info("Queueing work item in SynchronousEventSubscriber.");
            SUBSCRIBER.get(this).queueReceiveWork(work);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        consumer.close();
    }
}
