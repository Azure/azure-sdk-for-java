// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.ParallelTransferOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Tests EventProcessorClient.
 */
public class EventProcessorTest extends ServiceTest<EventProcessorOptions> {
    private static final String HEADERS = String.join("\t", "Id", "Index", "Count",
        "Elapsed Time (ns)", "Elapsed Time (s)", "Rate (ops/sec)");
    private static final String FORMAT_STRING = "%s\t%d\t%d\t%s\t%s\t%.2f";

    // Minimum duration is 2 minutes so we can give it time to claim all the partitions.
    private static final int MINIMUM_DURATION = 2 * 60;

    private final ConcurrentHashMap<String, SamplePartitionProcessor> partitionProcessorMap;
    private final Duration testDuration;

    private volatile long startTime;
    private volatile long endTime;
    private BlobContainerAsyncClient containerClient;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */

    public EventProcessorTest(EventProcessorOptions options) {
        super(options);

        partitionProcessorMap = new ConcurrentHashMap<>();

        // End the test 2 seconds
        testDuration = Duration.ofSeconds(options.getDuration() - 1);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        // It is the default duration or less than 2 minutes.
        if (options.getDuration() < MINIMUM_DURATION) {
            return Mono.error(new RuntimeException(
                "Test duration is too short. It should be at least " + MINIMUM_DURATION + " seconds"));
        }

        final String containerName = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"));

        containerClient = new BlobContainerClientBuilder()
            .connectionString(options.getStorageConnectionString())
            .containerName(containerName)
            .buildAsyncClient();

        final Mono<Void> createContainerMono = containerClient.exists().flatMap(exists -> {
            if (exists) {
                return containerClient.delete().then(containerClient.create());
            } else {
                return containerClient.create();
            }
        });

        return Mono.using(
            () -> createEventHubClientBuilder().buildAsyncProducerClient(),
            asyncClient -> {
                final Mono<Void> sendMessagesMono = asyncClient.getEventHubProperties()
                    .flatMapMany(properties -> {
                        for (String partitionId : properties.getPartitionIds()) {
                            partitionProcessorMap.put(partitionId, new SamplePartitionProcessor());
                        }

                        return Flux.fromIterable(properties.getPartitionIds());
                    })
                    .flatMap(partitionId -> {
                        if (options.publishMessages()) {
                            return sendMessages(asyncClient, partitionId, options.getEventsToSend());
                        } else {
                            return Mono.empty();
                        }
                    })
                    .then();

                return Mono.when(createContainerMono, sendMessagesMono);
            },
            EventHubProducerAsyncClient::close);
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        if (containerClient == null) {
            return Mono.error(new RuntimeException("ContainerClient should have been initialized."));
        }

        return Mono.usingWhen(
            Mono.fromCallable(() -> {
                System.out.println("Starting run.");

                final BlobCheckpointStore checkpointStore = new BlobCheckpointStore(containerClient);
                final EventProcessorClientBuilder builder = new EventProcessorClientBuilder()
                    .connectionString(options.getConnectionString(), options.getEventHubName())
                    .consumerGroup(options.getConsumerGroup())
                    .loadBalancingStrategy(LoadBalancingStrategy.GREEDY)
                    .checkpointStore(checkpointStore)
                    .processError(context -> {
                        final String partitionId = context.getPartitionContext().getPartitionId();
                        final SamplePartitionProcessor processor = partitionProcessorMap.get(partitionId);
                        if (processor == null) {
                            System.err.printf("partitionId: %s. No matching processor. onError: %s%n",
                                partitionId, context.getThrowable());
                        } else {
                            processor.onError(context.getPartitionContext(), context.getThrowable());
                        }
                    })
                    .processPartitionInitialization(context -> {
                        final String partitionId = context.getPartitionContext().getPartitionId();
                        final SamplePartitionProcessor processor = partitionProcessorMap.get(partitionId);
                        if (processor == null) {
                            System.err.printf("partitionId: %s. No matching processor. onOpen%n", partitionId);
                        } else {
                            processor.onOpen(context.getPartitionContext());
                        }
                    })
                    .processPartitionClose(context -> {
                        final String partitionId = context.getPartitionContext().getPartitionId();
                        final SamplePartitionProcessor processor = partitionProcessorMap.get(partitionId);
                        if (processor == null) {
                            System.err.printf("partitionId: %s. No matching processor. onClose%n", partitionId);
                        } else {
                            processor.onClose(context.getPartitionContext(), context.getCloseReason());
                        }
                    });

                if (options.isBatched()) {
                    if (options.getBatchSize() < 1) {
                        throw new RuntimeException("Batch size is invalid. " + options.getBatchSize());
                    }

                    builder.processEventBatch(context -> {
                        final String partitionId = context.getPartitionContext().getPartitionId();
                        final SamplePartitionProcessor processor = partitionProcessorMap.get(partitionId);
                        if (processor == null) {
                            System.err.printf("partitionId: %s. No matching processor. onEvent%n", partitionId);
                        } else {
                            processor.onEvents(context.getPartitionContext(), context.getEvents());
                        }
                    }, options.getBatchSize());
                } else {
                    builder.processEvent(context -> {
                        final String partitionId = context.getPartitionContext().getPartitionId();
                        final SamplePartitionProcessor processor = partitionProcessorMap.get(partitionId);
                        if (processor == null) {
                            System.err.printf("partitionId: %s. No matching processor. onEvent%n", partitionId);
                        } else {
                            processor.onEvents(context.getPartitionContext(), context.getEventData());
                        }
                    });
                }

                if (options.getTransportType() != null) {
                    builder.transportType(options.getTransportType());
                }

                return builder.buildEventProcessorClient();
            }),
            processor -> {
                startTime = System.nanoTime();
                processor.start();
                return Mono.delay(testDuration).then();
            },
            processor -> {
                endTime = System.nanoTime();

                System.out.println("Completed run.");
                return Mono.delay(Duration.ofMillis(500), Schedulers.boundedElastic())
                    .then(Mono.fromRunnable(() -> processor.stop()));
            });
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        System.out.println("Cleaning up.");

        if (containerClient != null) {
            final BlobAsyncClient blobAsyncClient = containerClient.getBlobAsyncClient("results.txt");
            final ArrayList<ByteBuffer> byteBuffers = new ArrayList<>();
            final ParallelTransferOptions options = new ParallelTransferOptions().setMaxConcurrency(4);

            outputPartitionResults(content -> {
                System.out.println(content);
                byteBuffers.add(ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8)));
            });

            return blobAsyncClient.upload(Flux.fromIterable(byteBuffers), options)
                .then()
                .doFinally(signal -> System.out.println("Done global clean up."));
        } else {
            outputPartitionResults(System.out::println);
            System.out.println("Done global clean up.");

            return Mono.empty();
        }
    }

    private void outputPartitionResults(Consumer<String> onOutput) {
        onOutput.accept(HEADERS);

        long total = 0;
        for (SamplePartitionProcessor processor : partitionProcessorMap.values()) {
            processor.onStop();
            final List<EventsCounter> counters = processor.getCounters();
            for (int i = 0; i < counters.size(); i++) {
                final EventsCounter eventsCounter = counters.get(i);
                total += eventsCounter.totalEvents();
                final String result = getResults(i, eventsCounter);
                onOutput.accept(result);
            }
        }

        double elapsedTime = (endTime - startTime) * 0.000000001;
        double eventsPerSecond = total / elapsedTime;

        onOutput.accept("");
        onOutput.accept(String.format("Total Events\t%d%n", total));
        onOutput.accept(String.format("Total Duration (s)\t%.2f%n", elapsedTime));
        onOutput.accept(String.format("Rate (events/s)\t%.2f%n", eventsPerSecond));
    }

    private String getResults(int index, EventsCounter eventsCounter) {
        final double seconds = eventsCounter.elapsedTime() * 0.000000001;
        final double operationsSecond = eventsCounter.totalEvents() / seconds;

        return String.format(FORMAT_STRING, eventsCounter.getPartitionId(), index,
            eventsCounter.totalEvents(), eventsCounter.elapsedTime(), seconds, operationsSecond);
    }
}
