// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

/**
 * Receives a single set of events then stops. {@link EventHubsOptions#getCount()} represents the batch size to
 * receive.
 */
public class ReceiveEventsTest extends ServiceTest {
    private PartitionReceiver receiver;
    private CompletableFuture<PartitionReceiver> receiverAsync;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public ReceiveEventsTest(EventHubsOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return Mono.usingWhen(
            Mono.fromCompletionStage(createEventHubClientAsync()),
            client -> sendMessages(client, options.getPartitionId(), getTotalNumberOfEventsPerPartition()),
            client -> Mono.fromCompletionStage(client.close()));
    }

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

    @Override
    public void run() {
        Objects.requireNonNull(options.getConsumerGroup(), "'getConsumerGroup' requires a value.");
        Objects.requireNonNull(options.getPartitionId(), "'getPartitionId' requires a value.");

        final Iterable<EventData> events;
        try {
            events = receiver.receiveSync(options.getCount());
        } catch (EventHubException e) {
            throw new RuntimeException("Unable to receive events.", e);
        }

        final int size = calculateNumberOfEvents(events);
        if (size != options.getCount()) {
            throw new RuntimeException(String.format("Did not get size. Expected: %s. Actual: %s%n.",
                options.getCount(), size));
        }
    }

    @Override
    public Mono<Void> runAsync() {
        Objects.requireNonNull(options.getConsumerGroup(), "'getConsumerGroup' requires a value.");
        Objects.requireNonNull(options.getPartitionId(), "'getPartitionId' requires a value.");
        return Mono.fromCompletionStage(
            receiverAsync.thenComposeAsync(receiver -> receiver.receive(options.getCount()))
                .thenAccept(events -> {
                    final int size = calculateNumberOfEvents(events);
                    if (size != options.getCount()) {
                        throw new RuntimeException(String.format("Did not get size. Expected: %s. Actual: %s%n.",
                            options.getCount(), size));
                    }
                }));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        if (options.isSync() && receiver != null) {
            return Mono.whenDelayError(
                Mono.fromCompletionStage(receiver.close()), super.cleanupAsync());
        } else if (receiverAsync != null) {
            return Mono.whenDelayError(
                Mono.fromCompletionStage(receiverAsync.thenComposeAsync(r -> r.close())),
                super.cleanupAsync());
        } else {
            return super.cleanupAsync();
        }
    }

    private static int calculateNumberOfEvents(Iterable<EventData> events) {
        long size = StreamSupport.stream(events.spliterator(), false).count();
        return Long.valueOf(size).intValue();
    }
}
