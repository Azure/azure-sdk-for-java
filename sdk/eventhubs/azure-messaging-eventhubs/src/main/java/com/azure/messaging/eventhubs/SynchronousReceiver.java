// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.WindowedSubscriber;
import com.azure.core.amqp.implementation.WindowedSubscriber.WindowedSubscriberOptions;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsConsumerInstrumentation;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;

/**
 * A type that channels synchronous receive requests to a backing asynchronous receiver client.
 */
final class SynchronousReceiver {
    private static final String TERMINAL_MESSAGE;
    private static final WindowedSubscriber<PartitionEvent> DISPOSED;
    static {
        TERMINAL_MESSAGE = "The receiver client is terminated. Re-create the client to continue receive attempt.";
        DISPOSED = Flux.<PartitionEvent>error(new RuntimeException("Disposed."))
            .subscribeWith(new WindowedSubscriber<>(new HashMap<>(0), TERMINAL_MESSAGE, new WindowedSubscriberOptions<>()));
    }
    private static final String SYNC_RECEIVE_SPAN_NAME = "EventHubs.receiveFromPartition";
    private final ClientLogger logger;
    private final EventHubConsumerAsyncClient asyncClient;
    private final EventHubsConsumerInstrumentation instrumentation;
    private final AtomicReference<WindowedSubscriber<PartitionEvent>> subscriber = new AtomicReference<>(null);

    /**
     * Creates a SynchronousReceiver.
     *
     * @param logger the logger to use.
     * @param asyncClient the backing asynchronous client to connect to the broker, delegate message requesting and receive.
     */
    SynchronousReceiver(ClientLogger logger, EventHubConsumerAsyncClient asyncClient) {
        this.logger = Objects.requireNonNull(logger, "'logger' cannot be null.");
        this.asyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
        this.instrumentation = asyncClient.getInstrumentation();
    }

    /**
     * Request a specified number of event and obtain an {@link IterableStream} streaming the received event.
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

        final WindowedSubscriber<PartitionEvent> s = subscriber.get();
        if (s != null) {
            return s.enqueueRequest(maxEvents, maxWaitTime);
        } else {
            return subscribeOnce(partitionId, startingPosition, receiveOptions).enqueueRequest(maxEvents, maxWaitTime);
        }
    }

    /**
     * Disposes the SynchronousReceiver.
     * <p>
     * Once disposed, the {@link IterableStream} for any future or pending receive requests will receive terminated error.
     * </p>
     */
    void dispose() {
        final WindowedSubscriber<PartitionEvent> s = subscriber.getAndSet(DISPOSED);
        if (s != null) {
            s.dispose();
        }
    }

    /**
     * Obtain a {@link WindowedSubscriber} that is subscribed to the asynchronous message stream produced by the backing
     * asynchronous client.
     * <p>
     * The method subscribes only once and cache the subscriber.
     * </p>
     * <p>
     * The subscriber exposes synchronous receive API and channels receive requests to the backing asynchronous message
     * stream.
     * </p>
     *
     * @param partitionId Identifier of the partition to read events from.
     * @param startingPosition Position within the Event Hub partition to begin consuming events.
     * @param receiveOptions Options when receiving events from the partition.
     *
     * @return the subscriber to channel synchronous receive requests to the upstream.
     */
    private WindowedSubscriber<PartitionEvent> subscribeOnce(String partitionId, EventPosition startingPosition,
        ReceiveOptions receiveOptions) {
        if (!asyncClient.isV2()) {
            throw logger.logExceptionAsError(new UnsupportedOperationException("SynchronousReceiver requires v2 mode."));
        }
        final WindowedSubscriber<PartitionEvent> s = createSubscriber(partitionId);
        if (subscriber.compareAndSet(null, s)) {
            // In case of concurrent invocation, the 's' created by the thread which lost the CAS race is eligible for GC.
            // There is no leak, as 's' is in mere constructed state and don’t own any leak-able resource yet.
            final Flux<PartitionEvent> upstream = asyncClient.receiveFromPartition(partitionId, startingPosition, receiveOptions);
            upstream.subscribeWith(s);
        }
        return subscriber.get();
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
            return toDecorate;
            //return instrumentation.reportSyncReceiveSpan(SYNC_RECEIVE_SPAN_NAME, startTime, toDecorate, Context.NONE);
        });
        return new WindowedSubscriber<>(Collections.singletonMap(PARTITION_ID_KEY, partitionId), TERMINAL_MESSAGE, options);
    }
}
