// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Receives a single set of events then stops. {@link EventHubsOptions#getCount()} represents the batch size to
 * receive.
 */
public class ReceiveEventsTests extends ServiceTest {
    private PartitionReceiver receiver;
    private CompletableFuture<PartitionReceiver> receiverAsync;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    ReceiveEventsTests(EventHubsOptions options) {
        super(options);
    }

    @Override
    public void run() {
        Objects.requireNonNull(options.getConsumerGroup(), "'getConsumerGroup' requires a value.");
        Objects.requireNonNull(options.getPartitionId(), "'getPartitionId' requires a value.");

        if (receiver == null) {
            try {
                client = createEventHubClient();
                receiver = client.createReceiverSync(options.getConsumerGroup(),
                    options.getPartitionId(), EventPosition.fromStartOfStream());
            } catch (EventHubException e) {
                throw new RuntimeException("Unable to create PartitionReceiver.", e);
            }
        }

        final Iterable<EventData> events;
        try {
            events = receiver.receiveSync(options.getCount());
        } catch (EventHubException e) {
            throw new RuntimeException("Unable to receive events.", e);
        }

        onReceive(events);
    }

    @Override
    public Mono<Void> runAsync() {
        Objects.requireNonNull(options.getConsumerGroup(), "'getConsumerGroup' requires a value.");
        Objects.requireNonNull(options.getPartitionId(), "'getPartitionId' requires a value.");

        if (receiverAsync == null) {
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

        return Mono.fromCompletionStage(
            receiverAsync.thenComposeAsync(receiver -> receiver.receive(options.getCount()))
                .thenAccept(events -> onReceive(events)));
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

    private static void onReceive(Iterable<EventData> events) {
        for (EventData event : events) {
            System.out.println("Sequence number: " + event.getSystemProperties().getSequenceNumber());
            System.out.println("Contents: " + new String(event.getBytes(), StandardCharsets.UTF_8));
        }
    }
}
