// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.WindowedSubscriber;
import com.azure.core.amqp.implementation.WindowedSubscriber.WindowedSubscriberOptions;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;

/**
 * A type that channels synchronous receive requests to a backing asynchronous receiver client.
 */
final class SynchronousPartitionReceiver {
    private static final String TERMINAL_MESSAGE = "The receiver client is terminated. Re-create the client to continue receive attempt.";
    private static final String SYNC_RECEIVE_SPAN_NAME = "EventHubs.receiveFromPartition";
    private final EventHubsTracer tracer;
    private final AtomicReference<Receiver> receiver = new AtomicReference<>(null);

    /**
     * Creates a SynchronousPartitionReceiver.
     *
     * @param client the backing asynchronous client to connect to the broker, delegate message requesting and receive.
     */
    SynchronousPartitionReceiver(EventHubConsumerAsyncClient client) {
        Objects.requireNonNull(client, "'client' cannot be null.");
        this.receiver.set(new DelegatingReceiver(client));
        this.tracer = client.getInstrumentation().getTracer();
    }

    /**
     * Request a specified number of event from a parition and obtain an {@link IterableStream} streaming the received
     * event.
     *
     * @param partitionId Identifier of the partition to read events from.
     * @param startingPosition Position within the Event Hub partition to begin consuming events.
     * @param receiveOptions Options when receiving events from the partition.
     * @param maxEvents the maximum number of event to receive.
     * @param maxWaitTime the upper bound for the time to wait to receive the requested number of event.
     *
     * @return an {@link IterableStream} of at most {@code maxEvents} event.
     */
    IterableStream<PartitionEvent> receive(String partitionId, EventPosition startingPosition,
        ReceiveOptions receiveOptions, int maxEvents, Duration maxWaitTime) {
        Objects.requireNonNull(partitionId, "'partitionId' cannot be null.");
        Objects.requireNonNull(startingPosition, "'startingPosition' cannot be null.");
        Objects.requireNonNull(receiveOptions, "'receiveOptions' cannot be null.");

        final WindowedSubscriber<PartitionEvent> subscriber = createSubscriber(partitionId);
        final Flux<PartitionEvent> upstream = receiver.get().receive(partitionId, startingPosition, receiveOptions);
        upstream.subscribeWith(subscriber);
        final Flux<PartitionEvent> windowFlux = subscriber.enqueueRequestFlux(maxEvents, maxWaitTime);
        return new IterableStream<>(windowFlux.doOnComplete(subscriber::cancel).doOnError(__ -> subscriber.cancel()));
    }

    /**
     * Disposes the SynchronousReceiver.
     * <p>
     * Once disposed, the {@link IterableStream} for any future or pending receive requests will receive terminated error.
     * </p>
     */
    void dispose() {
        receiver.set(Receiver.DISPOSED);
    }

    /**
     * Create a {@link WindowedSubscriber} capable of bridging synchronous receive requests to an upstream of
     * asynchronous event.
     *
     * @param partitionId Identifier of the partition to read events from.
     *
     * @return The subscriber.
     */
    private WindowedSubscriber<PartitionEvent> createSubscriber(String partitionId) {
        final WindowedSubscriberOptions<PartitionEvent> options = new WindowedSubscriberOptions<>();
        options.setWindowDecorator(toDecorate -> {
            // Decorates the provided 'toDecorate' flux for tracing the signals (events, termination) it produces.
            final Instant startTime = tracer.isEnabled() ? Instant.now() : null;
            return tracer.reportSyncReceiveSpan(SYNC_RECEIVE_SPAN_NAME, startTime, toDecorate, Context.NONE);
        });
        return new WindowedSubscriber<>(Collections.singletonMap(PARTITION_ID_KEY, partitionId), TERMINAL_MESSAGE, options);
    }

    private interface Receiver {
        Receiver DISPOSED = (partitionId, startingPosition, receiveOptions) -> Flux.error(new RuntimeException(TERMINAL_MESSAGE));

        Flux<PartitionEvent> receive(String partitionId, EventPosition startingPosition, ReceiveOptions receiveOptions);
    }

    private static final class DelegatingReceiver implements Receiver {
        private final EventHubConsumerAsyncClient client;

        DelegatingReceiver(EventHubConsumerAsyncClient client) {
            this.client = Objects.requireNonNull(client, "'client' cannot be null.");
        }

        @Override
        public Flux<PartitionEvent> receive(String partitionId, EventPosition startingPosition, ReceiveOptions receiveOptions) {
            assert client.isV2();
            return client.receiveFromPartition(partitionId, startingPosition, receiveOptions);
        }
    }
}
