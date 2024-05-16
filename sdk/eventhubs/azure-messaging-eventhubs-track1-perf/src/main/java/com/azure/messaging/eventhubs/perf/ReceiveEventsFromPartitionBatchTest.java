package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.perf.core.EventHubsPerfStressOptions;
import com.azure.messaging.eventhubs.perf.core.ServiceBatchTest;
import com.azure.messaging.eventhubs.perf.core.Util;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ReceiveEventsFromPartitionBatchTest extends ServiceBatchTest<EventHubsPerfStressOptions> {
    private PartitionReceiver receiver;
    protected byte[] eventDataBytes;

    /**
     * Instantiates instance of the Service Test.
     *
     * @param options The options bag to use to run performance test.
     * @throws IllegalStateException when expected configuration of environment variables is not found.
     */
    public ReceiveEventsFromPartitionBatchTest(EventHubsPerfStressOptions options) {
        super(options);
        try {
            receiver = eventHubClient.createReceiverSync(options.getConsumerGroup(),
                String.valueOf(options.getPartitionId()), EventPosition.fromStartOfStream());
        } catch (EventHubException e) {
            throw new RuntimeException("Unable to create PartitionReceiver.", e);
        }
        eventDataBytes = Util.generateString(options.getMessageSize()).getBytes(StandardCharsets.UTF_8);
    }


    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(Mono.defer(() -> Util.preLoadEvents(eventHubClient, options.getPartitionId() != null
                ? String.valueOf(options.getPartitionId()) : null , eventDataBytes, options.getEvents())));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(Mono.empty());
    }


    /**
     * Cleans up the receivers.
     *
     * @return A Mono that completes when the receivers are cleaned up and the scheduler shutdown..
     */
    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.fromFuture(receiver.close()).then(super.cleanupAsync());
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
        return Mono.fromFuture(receiver.receive(options.getCount()))
            .map(receivedEvents -> {
                int receivedSize = 0;
                for (EventData eventData : receivedEvents) {
                    Objects.requireNonNull(eventData, "'eventData' cannot be null");
                    receivedSize++;
                }
                return receivedSize;
            });
    }
}
