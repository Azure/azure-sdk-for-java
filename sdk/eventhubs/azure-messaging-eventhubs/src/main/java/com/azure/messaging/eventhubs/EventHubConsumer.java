// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import reactor.core.publisher.Flux;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/**
 * A consumer responsible for reading {@link EventData} from a specific Event Hub partition in the context of a specific
 * consumer group.
 *
 * <ul>
 * <li>If {@link EventHubConsumer} is created where {@link EventHubConsumerOptions#ownerLevel()} has a
 * value, then Event Hubs service will guarantee only one active consumer exists per partitionId and consumer group
 * combination. This consumer is sometimes referred to as an "Epoch Consumer."</li>
 * <li>Multiple consumers per partitionId and consumer group combination can be created by not setting
 * {@link EventHubConsumerOptions#ownerLevel()} when creating consumers. This non-exclusive consumer is sometimes
 * referred to as a "Non-Epoch Consumer."</li>
 * </ul>
 *
 * @see EventHubClient#createConsumer(String, String, EventPosition)
 * @see EventHubClient#createConsumer(String, String, EventPosition, EventHubConsumerOptions)
 */
public class EventHubConsumer implements Closeable {
    private final EventHubAsyncConsumer consumer;
    private final Flux<EventData> receiveEvents;
    private final Duration timeout;

    EventHubConsumer(EventHubAsyncConsumer consumer, EventHubConsumerOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        this.consumer = Objects.requireNonNull(consumer, "'consumer' cannot be null.");
        this.timeout = options.retry().tryTimeout();
        this.receiveEvents = this.consumer.receive();
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
            throw new IllegalArgumentException("'maximumMessageCount' cannot be less than 1.");
        } else if (maximumWaitTime.isNegative() || maximumWaitTime.isZero()) {
            throw new IllegalArgumentException("'maximumWaitTime' cannot be zero or less.");
        }

        final Flux<EventData> events = receiveEvents
            .windowTimeout(maximumMessageCount, timeout)
            .blockFirst(timeout);
        return new IterableStream<>(events);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        consumer.close();
    }
}
