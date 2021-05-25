// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Receives a single set of events then stops. {@link EventHubsOptions#getCount()} represents the batch size to
 * receive.
 */
public class ReceiveEventsTest extends ServiceTest<EventHubsReceiveOptions> {
    private EventHubConsumerClient receiver;
    private EventHubConsumerAsyncClient receiverAsync;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public ReceiveEventsTest(EventHubsReceiveOptions options) {
        super(options);
    }

    // @Override
    // public Mono<Void> globalSetupAsync() {
    //     return Mono.usingWhen(
    //         Mono.fromCompletionStage(createEventHubClientAsync()),
    //         client -> sendMessages(client, options.getPartitionId(), options.getCount()),
    //         client -> Mono.fromCompletionStage(client.close()));
    // }

    @Override
    public void run() {
        Objects.requireNonNull(options.getConsumerGroup(), "'getConsumerGroup' requires a value.");
        Objects.requireNonNull(options.getPartitionId(), "'getPartitionId' requires a value.");

        if (receiver == null) {
            receiver = createEventHubClientBuilder()
                .consumerGroup(options.getConsumerGroup())
                .buildConsumerClient();
        }

        final IterableStream<PartitionEvent> partitionEvents = receiver.receiveFromPartition(
            options.getPartitionId(), options.getCount(), EventPosition.earliest());

        partitionEvents.forEach(partitionEvent -> onReceive(partitionEvent));
    }

    @Override
    public Mono<Void> runAsync() {
        Objects.requireNonNull(options.getConsumerGroup(), "'getConsumerGroup' requires a value.");
        Objects.requireNonNull(options.getPartitionId(), "'getPartitionId' requires a value.");

        if (receiverAsync == null) {
            receiverAsync = createEventHubClientBuilder()
                .consumerGroup(options.getConsumerGroup())
                .buildAsyncConsumerClient();
        }

        return receiverAsync.receiveFromPartition(options.getPartitionId(), EventPosition.earliest())
            .take(options.getCount())
            .map(event -> {
                onReceive(event);
                return event;
            })
            .then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        if (options.isSync() && receiver != null) {
            return Mono.fromRunnable(() -> receiver.close());
        } else if (receiverAsync != null) {
            return Mono.fromRunnable(() -> receiverAsync.close());
        } else {
            return super.cleanupAsync();
        }
    }

    private static void onReceive(PartitionEvent partitionEvent) {
        EventData event = partitionEvent.getData();
        System.out.println("Sequence number: " + event.getSequenceNumber());
        System.out.println("Contents: " + event.getBodyAsString());
    }
}
