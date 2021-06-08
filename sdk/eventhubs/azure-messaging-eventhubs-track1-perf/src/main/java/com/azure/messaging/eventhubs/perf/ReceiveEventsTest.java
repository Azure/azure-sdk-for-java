// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Receives a single set of events then stops. {@link EventHubsOptions#getCount()} represents the batch size to
 * receive.
 */
public class ReceiveEventsTest extends ServiceTest<EventHubsReceiveOptions> {
    private PartitionReceiver receiver;
    private CompletableFuture<PartitionReceiver> receiverAsync;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public ReceiveEventsTest(EventHubsReceiveOptions options) {
        super(options);

        Objects.requireNonNull(options.getConsumerGroup(), "'getConsumerGroup' requires a value.");
        Objects.requireNonNull(options.getPartitionId(), "'getPartitionId' requires a value.");
    }

    /**
     * Creates a client and sends messages to the given {@link EventHubsOptions#getPartitionId()}.
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
        if (options.isSync() && receiver == null) {
            try {
                client = createEventHubClient();
                receiver = client.createReceiverSync(options.getConsumerGroup(),
                    options.getPartitionId(), EventPosition.fromStartOfStream());
            } catch (EventHubException e) {
                throw new RuntimeException("Unable to create PartitionReceiver.", e);
            }
        } else if (!options.isSync() && receiverAsync == null) {
            clientFuture = createEventHubClientAsync();
            receiverAsync = clientFuture.thenComposeAsync(client -> {
                try {
                    return client.createReceiver(options.getConsumerGroup(),
                        options.getPartitionId(), EventPosition.fromStartOfStream());
                } catch (EventHubException e) {
                    final CompletableFuture<PartitionReceiver> future = new CompletableFuture<>();
                    future.completeExceptionally(new RuntimeException("Unable to create PartitionReceiver", e));
                    return future;
                }
            });
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
        final CompletableFuture<Void> receiveEvents = receiverAsync.thenApplyAsync(receiver -> {
            final AtomicInteger number = new AtomicInteger();
            while (true) {
                try {
                    final Iterable<EventData> receivedEvents = receiver.receive(options.getCount()).get();
                    for (EventData eventData : receivedEvents) {
                        number.incrementAndGet();
                    }

                    if (number.get() >= options.getCount()) {
                        break;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Unable to get more events", e);
                }
            }

            return null;
        });

        return Mono.fromCompletionStage(receiveEvents);
    }

    /**
     * Cleans up the receivers.
     *
     * @return A Mono that completes when the receivers are cleaned up and the scheduler shutdown..
     */
    @Override
    public Mono<Void> cleanupAsync() {
        if (receiver != null) {
            return Mono.whenDelayError(
                Mono.fromCompletionStage(receiver.close()), super.cleanupAsync());
        } else if (receiverAsync != null) {
            return Mono.whenDelayError(
                Mono.fromCompletionStage(receiverAsync.thenComposeAsync(r -> r.close())).doFinally(signal -> {
                    System.out.println("Done async receiver clean. Signal: " + signal);
                }).timeout(Duration.ofSeconds(45)),
                super.cleanupAsync().doFinally(signal -> {
                    System.out.println("Done super.clean() clean. Signal: " + signal);
                }));
        } else {
            return super.cleanupAsync();
        }
    }
}
