// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import reactor.core.publisher.Mono;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EventProcessorTest extends ServiceTest {
    private final SampleEventProcessorFactory processorFactory;
    private final ConcurrentHashMap<String, SamplePartitionProcessor> partitionProcessorMap;
    private final InMemoryCheckpointManager checkpointManager;
    private final InMemoryLeaseManager leaseManager;
    private final Duration testDuration;
    private final ClientLogger logger = new ClientLogger(EventProcessorTest.class);

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public EventProcessorTest(EventHubsOptions options) {
        super(options);

        partitionProcessorMap = new ConcurrentHashMap<>();
        processorFactory = new SampleEventProcessorFactory(partitionProcessorMap);
        checkpointManager = new InMemoryCheckpointManager();
        leaseManager = new InMemoryLeaseManager("test-host");

        // End the test 2 seconds
        testDuration = Duration.ofSeconds(options.getDuration() - 1);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return Mono.usingWhen(
            Mono.fromCompletionStage(createEventHubClientAsync()),
            client -> Mono.fromCompletionStage(client.getRuntimeInformation())
                .flatMap(runtimeInformation -> {
                    for (String id : runtimeInformation.getPartitionIds()) {
                        partitionProcessorMap.put(id, new SamplePartitionProcessor());
                    }

                    final List<Mono<Void>> allSends = Arrays.stream(runtimeInformation.getPartitionIds())
                        .map(id -> sendMessages(client, id, getTotalNumberOfEventsPerPartition()))
                        .collect(Collectors.toList());

                    return Mono.when(allSends);
                }),
            client -> Mono.fromCompletionStage(client.close()));
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        final Mono<EventProcessorHost> createProcessor = Mono.defer(() -> {
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

        final Mono<Long> timeout = Mono.delay(testDuration);
        return Mono.usingWhen(
            createProcessor,
            processor -> Mono.when(timeout),
            processor -> Mono.fromCompletionStage(processor.unregisterEventProcessor()));
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return Mono.fromRunnable(() -> {
            if (options.getOutputFile() == null) {
                for (SamplePartitionProcessor processor : partitionProcessorMap.values()) {
                    final String results = processor.getResults();
                    logger.info(results);
                }

                return;
            }

            try (FileWriter writer = new FileWriter(options.getOutputFile())) {
                for (SamplePartitionProcessor processor : partitionProcessorMap.values()) {
                    final String results = processor.getResults();
                    writer.write(results);
                }
            } catch (IOException e) {
                logger.warning("Unable to write to file: {}", options.getOutputFile(), e);
            }
        }).then(super.globalCleanupAsync());
    }
}
