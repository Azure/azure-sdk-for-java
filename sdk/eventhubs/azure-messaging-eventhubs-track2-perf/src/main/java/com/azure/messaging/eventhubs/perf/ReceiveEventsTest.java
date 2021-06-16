// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Receives a single set of events then stops. {@link EventHubsOptions#getCount()} represents the batch size to
 * receive.
 */
public class ReceiveEventsTest extends ServiceTest<EventHubsReceiveOptions> {
    private EventHubConsumerClient receiver;
    private EventHubConsumerAsyncClient receiverAsync;
    private final int totalMessagesToSend;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public ReceiveEventsTest(EventHubsReceiveOptions options) {
        super(options);
        this.totalMessagesToSend = options.getCount() * 2;
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return Mono.using(
            () -> createEventHubClientBuilder().buildAsyncProducerClient(),
            client -> sendMessages(client, options.getPartitionId(), totalMessagesToSend),
            client -> client.close());
    }

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

        // Force the evaluation of the iterable stream.
        final List<PartitionEvent> results = partitionEvents.stream().collect(Collectors.toList());

        if (results.isEmpty()) {
            throw new RuntimeException("Did not receive any events.");
        } else if (results.size() != options.getCount()) {
            throw new RuntimeException(String.format(
                "Did not receive correct number of events. Expected: %d. Actual: %d.", options.getCount(),
                results.size()));
        }
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
            .then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        if (receiver != null) {
            receiver.close();
        }
        if (receiverAsync != null) {
            receiverAsync.close();
        }

        return super.cleanupAsync();
    }
}
