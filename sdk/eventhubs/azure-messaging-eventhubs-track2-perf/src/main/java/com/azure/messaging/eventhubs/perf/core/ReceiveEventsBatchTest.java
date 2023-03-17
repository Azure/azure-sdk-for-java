package com.azure.messaging.eventhubs.perf.core;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class ReceiveEventsBatchTest extends ServiceBatchTest<EventHubsPerfOptions> {
    private EventHubConsumerClient eventHubConsumerClient;
    private EventHubConsumerAsyncClient eventHubConsumerAsyncClient;
    private byte[] eventDataBytes;

    /**
     * Instantiates instance of the Service Test.
     *
     * @param options The options bag to use to run performance test.
     * @throws IllegalStateException when expected configuration of environment variables is not found.
     */
    public ReceiveEventsBatchTest(EventHubsPerfOptions options) throws IllegalStateException {
        super(options);
        if(options.getPartitionId() != null) {
            throw new IllegalStateException("Partition Id not required/supported for this test case.");
        }
        eventDataBytes = Util.generateString(options.getMessageSize()).getBytes(StandardCharsets.UTF_8);
    }


    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(Mono.defer(() -> Util.preLoadEvents(eventHubProducerAsyncClient, options.getPartitionId() != null
                ? String.valueOf(options.getPartitionId()) : null , options.getEvents(), eventDataBytes)));
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
        throw new UnsupportedOperationException("Operation not supported.");
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        int receiveCount = options.getCount();
        return Mono.using(
            eventHubClientBuilder::buildAsyncConsumerClient,
            consumerAsyncClient -> consumerAsyncClient.receive(true)
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
