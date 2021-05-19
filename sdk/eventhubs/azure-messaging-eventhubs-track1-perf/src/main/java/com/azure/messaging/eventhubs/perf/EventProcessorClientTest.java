// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.eventprocessorhost.ICheckpointManager;
import com.microsoft.azure.eventprocessorhost.ILeaseManager;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class EventProcessorClientTest extends ServiceTest {
    private SampleEventProcessorFactory processorFactory;
    private ConcurrentHashMap<String, CountDownLatch> eventsToReceive;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public EventProcessorClientTest(EventHubsOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        eventsToReceive = new ConcurrentHashMap<>();
        processorFactory = new SampleEventProcessorFactory(eventsToReceive);

        return Mono.usingWhen(
            Mono.fromCompletionStage(createEventHubClientAsync()),
            client -> {
                return Mono.fromCompletionStage(client.getRuntimeInformation())
                    .flatMap(runtimeInformation -> {
                        for (String id : runtimeInformation.getPartitionIds()) {
                            eventsToReceive.put(id, new CountDownLatch(options.getCount()));
                        }

                        final List<Mono<Void>> allSends = Arrays.stream(runtimeInformation.getPartitionIds())
                            .map(id -> sendMessages(client, id, getTotalNumberOfEventsPerPartition()))
                            .collect(Collectors.toList());

                        return Mono.when(allSends);
                    });
            },
            client -> Mono.fromCompletionStage(client.close()));
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        // Reset the countdown events.
        eventsToReceive.keySet().forEach(key -> {
            eventsToReceive.put(key, new CountDownLatch(options.getCount()));
        });
        processorFactory = new SampleEventProcessorFactory(eventsToReceive);

        final Mono<EventProcessorHost> createProcessor = Mono.defer(() -> {
            final ConcurrentHashMap<String, OwnershipInformation> partitionOwnershipMap = new ConcurrentHashMap<>();
            final ICheckpointManager checkpointManager = new SampleCheckpointManager(partitionOwnershipMap);
            final ILeaseManager leaseManager = new SampleLeaseManager(partitionOwnershipMap);
            final ConnectionStringBuilder connectionStringBuilder = getConnectionStringBuilder();
            final EventProcessorHost.EventProcessorHostBuilder.OptionalStep builder =
                EventProcessorHost.EventProcessorHostBuilder.newBuilder(
                    connectionStringBuilder.getEndpoint().toString(), options.getConsumerGroup())
                    .useUserCheckpointAndLeaseManagers(checkpointManager, leaseManager)
                    .useEventHubConnectionString(connectionStringBuilder.toString())
                    .setExecutor(getScheduler());

            final EventProcessorHost processor = builder.build();
            return Mono.fromCompletionStage(processor.registerEventProcessorFactory(processorFactory))
                .thenReturn(processor);
        });

        return Mono.usingWhen(
            createProcessor,
            processor -> {
                final List<Mono<Void>> waitOperations = eventsToReceive.entrySet()
                    .stream()
                    .map(entry -> {
                        return Mono.<Void>fromRunnable(() -> {
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
            processor -> Mono.fromCompletionStage(processor.unregisterEventProcessor()));
    }
}
