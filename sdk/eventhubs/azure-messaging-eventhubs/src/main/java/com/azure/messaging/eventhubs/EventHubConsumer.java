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
    private final EventHubConsumerOptions options;

    EventHubConsumer(EventHubAsyncConsumer consumer, EventHubConsumerOptions options) {
        this.consumer = Objects.requireNonNull(consumer);
        this.options = Objects.requireNonNull(options);

        //TODO (conniey): Keep track of the last sequence number as each method invoked.
        this.consumer.receive().windowTimeout(options.prefetchCount(), this.options.retry().tryTimeout());
    }

    /**
     * Receives a batch of EventData from the Event Hub partition.
     *
     * @param maximumMessageCount The maximum number of messages to receive in this batch.
     * @return A set of {@link EventData} that was received. The iterable contains up to {@code maximumMessageCount}
     *     events.
     */
    public IterableStream<EventData> receive(int maximumMessageCount) {
        return new IterableStream<>(Flux.empty());
    }

    /**
     * Receives a batch of EventData from the Event Hub partition
     *
     * @param maximumMessageCount The maximum number of messages to receive in this batch.
     * @param maximumWaitTime The maximum amount of time to wait to build up the requested message count for the
     *     batch; if not specified, the default wait time specified when the consumer was created will be used.
     * @return A set of {@link EventData} that was received. The iterable contains up to {@code maximumMessageCount}
     *     events.
     */
    public IterableStream<EventData> receive(int maximumMessageCount, Duration maximumWaitTime) {
        return new IterableStream<>(Flux.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        consumer.close();
    }
}
