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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class EventProcessorClientTest extends ServiceTest {
    private final ConcurrentHashMap<String, OwnershipInformation> partitionOwnershipMap = new ConcurrentHashMap<>();
    private final ICheckpointManager checkpointManager = new SampleCheckpointManager(partitionOwnershipMap);
    private final ILeaseManager leaseManager = new SampleLeaseManager(partitionOwnershipMap);
    private final SampleEventProcessorFactory processorFactory;
    private final ConcurrentHashMap<String, CountDownLatch> eventsToReceive = new ConcurrentHashMap<>();

    private EventProcessorHost eventProcessorHost;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public EventProcessorClientTest(EventHubsOptions options) {
        super(options);

        processorFactory = new SampleEventProcessorFactory(eventsToReceive);
    }

    @Override
    public Mono<Void> setupAsync() {
        final ConnectionStringBuilder connectionStringBuilder = getConnectionStringBuilder();
        final EventProcessorHost.EventProcessorHostBuilder.OptionalStep builder =
            EventProcessorHost.EventProcessorHostBuilder.newBuilder(
                connectionStringBuilder.getEndpoint().toString(), options.getConsumerGroup())
                .useUserCheckpointAndLeaseManagers(checkpointManager, leaseManager)
                .useEventHubConnectionString(connectionStringBuilder.toString())
                .setExecutor(getScheduler());

        eventProcessorHost = builder.build();

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
        eventProcessorHost.registerEventProcessorFactory(processorFactory);

        try {
            eventsToReceive.forEach((key, value) -> {
                try {
                    value.wait();
                } catch (InterruptedException e) {
                    System.err.printf("Could not wait on countdown for partitionId: %s. Error: %s%n", key, e);
                }
            });
        } finally {
            try {
                eventProcessorHost.unregisterEventProcessor().get();
            } catch (InterruptedException | ExecutionException e) {
                System.err.printf("Could not unregister partition processor. Error: %s%n", e);
            }
        }
    }

    @Override
    public Mono<Void> runAsync() {
        final CompletableFuture<Void> execution = CompletableFuture.runAsync(() -> {
            eventProcessorHost.registerEventProcessorFactory(processorFactory);
            eventsToReceive.forEach((key, value) -> {
                try {
                    value.wait();
                } catch (InterruptedException e) {
                    System.err.printf("Could not wait on countdown for partitionId: %s. Error: %s%n", key, e);
                }
            });
        }).handleAsync((empty, error) -> {
            eventProcessorHost.unregisterEventProcessor();
            return empty;
        });

        return Mono.fromCompletionStage(execution);
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
