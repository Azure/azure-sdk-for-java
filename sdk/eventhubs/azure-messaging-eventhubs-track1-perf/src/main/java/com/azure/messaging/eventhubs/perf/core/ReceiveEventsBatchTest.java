package com.azure.messaging.eventhubs.perf.core;

import com.microsoft.azure.eventhubs.*;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class ReceiveEventsBatchTest extends ServiceBatchTest<EventHubsPerfStressOptions> {
    private PartitionReceiver receiver;

    /**
     * Instantiates instance of the Service Test.
     *
     * @param options The options bag to use to run performance test.
     * @throws IllegalStateException when expected configuration of environment variables is not found.
     */
    public ReceiveEventsBatchTest(EventHubsPerfStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> setupAsync() {
        return Mono.fromCallable(() -> {
            try {
                receiver = eventHubClient.createReceiverSync(options.getConsumerGroup(),
                    String.valueOf(options.getPartitionId()), EventPosition.fromStartOfStream());
            } catch (EventHubException e) {
                throw new RuntimeException("Unable to create PartitionReceiver.", e);
            }
            return 1;
        }).then(Mono.defer(() -> sendMessages(eventHubClient, String.valueOf(options.getPartitionId()), options.getEvents())));
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
                Mono.fromCompletionStage(receiver.close()), super.cleanupAsync());
        } else {
            return Mono.empty();
        }
    }
    @Override
    public int runBatch() {
        try {
            final Iterable<EventData> receivedEvents = receiver.receiveSync(options.getCount());
            int receivedSize = 0;
            for (EventData eventData : receivedEvents) {
                Objects.requireNonNull(eventData, "'eventData' cannot be null");
                receivedSize++;
            }
            return receivedSize;
        } catch (EventHubException e) {
            throw new RuntimeException("Unable to get more events", e);
        }
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        return Mono.error(new RuntimeException("Unsupported Operation."));
    }
}
