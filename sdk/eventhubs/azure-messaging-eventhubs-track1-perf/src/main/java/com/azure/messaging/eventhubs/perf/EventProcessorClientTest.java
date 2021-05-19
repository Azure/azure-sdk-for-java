// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.eventprocessorhost.ICheckpointManager;
import com.microsoft.azure.eventprocessorhost.ILeaseManager;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    public Mono<Void> setupAsync() {
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
                            .map(id -> sendMessages(client, id))
                            .collect(Collectors.toList());

                        return Mono.when(allSends);
                    });
            },
            client -> Mono.fromCompletionStage(client.close()));
    }

    @Override
    public void run() {
    }

    @Override
    public Mono<Void> runAsync() {
        Mono<EventProcessorHost> createProcessor = Mono.defer(() -> {
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

    private Mono<Void> sendMessages(EventHubClient client, String partitionId) {
        CompletableFuture<PartitionSender> createSenderFuture;
        try {
            createSenderFuture = client.createPartitionSender(partitionId);
        } catch (EventHubException e) {
            createSenderFuture = new CompletableFuture<>();
            createSenderFuture.completeExceptionally(
                new RuntimeException("Unable to create partition sender: " + partitionId, e));
        }

        return Mono.usingWhen(
            Mono.fromCompletionStage(createSenderFuture),
            sender -> {
                final ArrayList<EventDataBatch> batches = new ArrayList<>();
                int numberOfMessages = options.getCount();
                EventDataBatch currentBatch;

                while (numberOfMessages > 0) {
                    currentBatch = sender.createBatch();
                    addEvents(currentBatch, numberOfMessages);
                    batches.add(currentBatch);
                    numberOfMessages = numberOfMessages - currentBatch.getSize();
                }

                final List<Mono<Void>> sends = batches.stream()
                    .map(batch -> Mono.fromCompletionStage(sender.send(batch)))
                    .collect(Collectors.toList());

                return Mono.when(sends);
            },
            sender -> Mono.fromCompletionStage(sender.close()));
    }
}
