package com.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class ReceiveEventsFromPartitionBatchTest extends ServiceBatchTest<EventHubsPerfOptions> {
    private EventHubConsumerClient eventHubConsumerClient;
    private EventHubConsumerAsyncClient eventHubConsumerAsyncClient;

    /**
     * Instantiates instance of the Service Test.
     *
     * @param options The options bag to use to run performance test.
     * @throws IllegalStateException when expected configuration of environment variables is not found.
     */
    public ReceiveEventsFromPartitionBatchTest(EventHubsPerfOptions options) throws IllegalStateException {
        super(options);
        if(options.getPartitionId() == null) {
            throw new IllegalStateException("Specify target partition id.");
        }
    }


    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(Mono.defer(() -> preLoadEvents(eventHubProducerAsyncClient, String.valueOf(options.getPartitionId()), options.getEvents())));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(Mono.fromCallable(() -> {
            // Setup the service client
            eventHubClientBuilder = new EventHubClientBuilder().connectionString(connectionString, eventHubName);
            eventHubClientBuilder
                .prefetchCount(options.getPrefetch())
                .consumerGroup(options.getConsumerGroup());
            eventHubConsumerClient = eventHubClientBuilder.buildConsumerClient();
            eventHubConsumerAsyncClient = eventHubClientBuilder.buildAsyncConsumerClient();
            return 1;
        })).then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.fromCallable(() -> {
            eventHubConsumerClient.close();
            eventHubConsumerAsyncClient.close();
            return 1;
        }).then(Mono.defer(() -> super.cleanupAsync()));
    }

    @Override
    public int runBatch() {
        int count = 0;
        final IterableStream<PartitionEvent> partitionEvents = eventHubConsumerClient.receiveFromPartition(
            String.valueOf(options.getPartitionId()), options.getCount(), EventPosition.earliest());

        // Force the evaluation of the iterable stream.
        final List<PartitionEvent> results = partitionEvents.stream().collect(Collectors.toList());

        if (results.isEmpty()) {
            throw new RuntimeException("Did not receive any events.");
        }
        return results.size();
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        int receiveCount = options.getCount();
        return Mono.using(
            eventHubClientBuilder::buildAsyncConsumerClient,
            consumerAsyncClient -> consumerAsyncClient.receiveFromPartition(
                String.valueOf(options.getPartitionId()), EventPosition.earliest())
                .take(receiveCount)
                .flatMap(message -> {
                    return Mono.empty();
                }, 1)
                .then()
                .thenReturn(receiveCount),
            EventHubConsumerAsyncClient::close,
            true
        );
    }
}
