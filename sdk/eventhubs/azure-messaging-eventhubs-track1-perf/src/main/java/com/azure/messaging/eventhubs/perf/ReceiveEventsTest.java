// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.ReceiverOptions;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.StreamSupport;

/**
 * Receives a single set of events then stops. {@link EventHubsReceiveOptions#getCount()} represents the batch size to
 * receive.
 */
public class ReceiveEventsTest extends ServiceTest<EventHubsReceiveOptions> {
    private PartitionReceiver receiver;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public ReceiveEventsTest(EventHubsReceiveOptions options) {
        super(options);
    }

    /**
     * Creates a client and sends messages to the given {@link EventHubsReceiveOptions#getPartitionId()}.
     *
     * @return A Mono that completes when messages are sent.
     */
    @Override
    public Mono<Void> globalSetupAsync() {
        return Mono.usingWhen(
            Mono.fromCompletionStage(createEventHubClientAsync()),
            client -> sendMessages(client, options.getPartitionId(), options.getCount()),
            client -> Mono.fromCompletionStage(client.close()));
    }

    /**
     * Creates either a sync or async receiver instance.
     *
     * @return Mono that completes when the receiver futures methods are hooked up.
     */
    @Override
    public Mono<Void> setupAsync() {
        if (options.isSync()) {
            try {
                client = createEventHubClient();
                receiver = client.createReceiverSync(options.getConsumerGroup(),
                    options.getPartitionId(), EventPosition.fromStartOfStream());
            } catch (EventHubException e) {
                throw new RuntimeException("Unable to create PartitionReceiver.", e);
            }
        } else {
            clientFuture = createEventHubClientAsync();
        }

        return Mono.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        final AtomicInteger number = new AtomicInteger();
        while (true) {
            try {
                final Iterable<EventData> receivedEvents = receiver.receiveSync(options.getCount());
                for (EventData eventData : receivedEvents) {
                    Objects.requireNonNull(eventData, "'eventData' cannot be null");
                    number.incrementAndGet();
                }

                if (number.get() >= options.getCount()) {
                    break;
                }
            } catch (EventHubException e) {
                throw new RuntimeException("Unable to get more events", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> runAsync() {
        final EventsHandler handler = new EventsHandler(options.getCount(), options.getPrefetch());
        final CompletableFuture<PartitionReceiver> partitionReceiver = createReceiver();
        final CompletableFuture<Void> receiveOperation = partitionReceiver
            .thenCompose(receiver -> receiver.setReceiveHandler(handler))
            .thenCompose(unused -> handler.isCompleteReceiving());

        return Mono.fromCompletionStage(receiveOperation
            .thenCompose(unused -> partitionReceiver)
            .thenCompose(PartitionReceiver::close));
    }

    /**
     * Cleans up the receivers.
     *
     * @return A Mono that completes when the receivers are cleaned up and the scheduler shutdown..
     */
    @Override
    public Mono<Void> cleanupAsync() {
        if (options.isSync()) {
            return Mono.whenDelayError(
                Mono.fromCompletionStage(client.close()), super.cleanupAsync());
        } else {
            return Mono.whenDelayError(
                Mono.fromCompletionStage(clientFuture.thenCompose(EventHubClient::close)),
                super.cleanupAsync());
        }
    }

    private CompletableFuture<PartitionReceiver> createReceiver() {
        return clientFuture.thenComposeAsync(client -> {
            try {
                final ReceiverOptions receiverOptions = new ReceiverOptions();
                receiverOptions.setPrefetchCount(options.getPrefetch());

                return client.createReceiver(options.getConsumerGroup(),
                    options.getPartitionId(), EventPosition.fromStartOfStream(), receiverOptions);
            } catch (EventHubException e) {
                final CompletableFuture<PartitionReceiver> future = new CompletableFuture<>();
                future.completeExceptionally(new RuntimeException("Unable to create PartitionReceiver", e));
                return future;
            }
        });
    }

    private static final class EventsHandler implements PartitionReceiveHandler {
        private final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        private final AtomicLong numberOfEvents;
        private final int prefetch;

        private EventsHandler(int numberOfEvents, int prefetch) {
            this.numberOfEvents = new AtomicLong(numberOfEvents);
            this.prefetch = prefetch;
        }

        @Override
        public int getMaxEventCount() {
            return prefetch;
        }

        @Override
        public void onReceive(Iterable<EventData> events) {
            if (events == null) {
                return;
            }

            final long count = StreamSupport.stream(events.spliterator(), false).count();
            final long left = numberOfEvents.addAndGet(-count);

            if (left <= 0) {
                completableFuture.complete(null);
            }
        }

        @Override
        public void onError(Throwable error) {
            completableFuture.completeExceptionally(error);
        }

        /**
         * Completes when the total number of events are received.
         *
         * @return A future that completes when all items are received.
         */
        public CompletableFuture<Void> isCompleteReceiving() {
            return completableFuture;
        }
    }
}
