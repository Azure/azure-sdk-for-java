// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Tests EventProcessorClient.
 */
public class EventProcessorTest extends ServiceTest {
    private final ConcurrentHashMap<String, CountDownLatch> eventsToReceive = new ConcurrentHashMap<>();

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public EventProcessorTest(EventHubsOptions options) {
        super(options);

    }

    @Override
    public Mono<Void> setupAsync() {
        return Mono.using(
            () -> createEventHubClientBuilder().buildAsyncProducerClient(),
            asyncClient -> {
                return asyncClient.getEventHubProperties()
                    .flatMapMany(properties -> {
                        for (String partitionId : properties.getPartitionIds()) {
                            eventsToReceive.put(partitionId, new CountDownLatch(options.getCount()));
                        }

                        return Flux.fromIterable(properties.getPartitionIds());
                    })
                    .flatMap(partitionId -> sendMessages(asyncClient, partitionId))
                    .then();
            },
            asyncClient -> asyncClient.close());
    }

    @Override
    public void run() {
    }

    @Override
    public Mono<Void> cleanupAsync() {
        eventsToReceive.clear();
        return super.cleanupAsync();
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.using(
            () -> {
                final EventProcessorClientBuilder builder = new EventProcessorClientBuilder()
                    .connectionString(options.getConnectionString())
                    .consumerGroup(options.getConsumerGroup())
                    .loadBalancingStrategy(LoadBalancingStrategy.GREEDY)
                    .checkpointStore(new SampleCheckpointStore());

                return builder.buildEventProcessorClient();
            },
            processor -> {
                final List<Mono<Object>> waitOperations = eventsToReceive.entrySet()
                    .stream()
                    .map(entry -> {
                        return Mono.fromRunnable(() -> {
                            try {
                                entry.getValue().await();
                            } catch (InterruptedException e) {
                                throw new RuntimeException("Unable to wait for entry to finish: " + entry.getKey(), e);
                            }
                        });
                    })
                    .collect(Collectors.toList());

                return Mono.when(waitOperations);
            },
            processor -> processor.stop());
    }

    private Mono<Void> sendMessages(EventHubProducerAsyncClient client, String partitionId) {
        final AtomicInteger numberOfMessages = new AtomicInteger(options.getCount());
        final CreateBatchOptions batchOptions = new CreateBatchOptions()
            .setPartitionId(partitionId);

        return Mono.when(client.createBatch(batchOptions)
            .map(batch -> {
                addEvents(batch, numberOfMessages.get());
                numberOfMessages.getAndAdd(-batch.getCount());
                return batch;
            })
            .repeat(() -> numberOfMessages.get() > 0)
            .flatMap(batch -> client.send(batch)));
    }
}
