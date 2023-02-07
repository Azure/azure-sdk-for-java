// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.EventPerfTest;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.blob.BlobOutputStream;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Tests Event Processor Host.
 */
public class EventProcessorTest extends EventPerfTest<EventProcessorOptions> {
    private static final String STORAGE_PREFIX = "perf";
    private static final String HEADERS = String.join("\t", "Id", "Index", "Count",
        "Elapsed Time (ns)", "Elapsed Time (s)", "Rate (ops/sec)");
    private static final String FORMAT_STRING = "%s\t%d\t%d\t%s\t%s\t%.2f";

    // Minimum duration is 2 minutes, so we can give it time to claim all the partitions.
    private static final int MINIMUM_DURATION = 2 * 60;

    private final EventHubsTestHelper<EventHubsOptions> testHelper;

    private final SampleEventProcessorFactory processorFactory;
    private final ConcurrentHashMap<String, SamplePartitionProcessor> partitionProcessorMap;

    private String containerName;
    private StorageCredentials storageCredentials;
    private CloudBlobContainer containerReference;
    private EventProcessorHost processor;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public EventProcessorTest(EventProcessorOptions options) {
        super(options);

        this.partitionProcessorMap = new ConcurrentHashMap<>();
        this.processorFactory = new SampleEventProcessorFactory(partitionProcessorMap);
        this.testHelper = new EventHubsTestHelper<>(options);
    }

    /**
     * Sends {@link EventProcessorOptions#getEventsToSend()} number of events to each partition.
     */
    @Override
    public Mono<Void> globalSetupAsync() {
        // It is the default duration or less than 2 minutes.
        if (options.getDuration() < MINIMUM_DURATION) {
            return Mono.error(new RuntimeException(
                "Test duration is too short. It should be at least " + MINIMUM_DURATION + " seconds"));
        }

        try {
            storageCredentials = StorageCredentials.tryParseCredentials(options.getStorageConnectionString());
            containerName = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHMMss"));

            final StorageUri storageUri = new StorageUri(URI.create(options.getStorageEndpoint()));
            final CloudBlobClient client = new CloudBlobClient(storageUri, storageCredentials);
            containerReference = client.getContainerReference(containerName);

            if (containerReference.deleteIfExists()) {
                System.out.printf("Deleting %s because it existed before.%n", containerName);
            }

            containerReference.create();
        } catch (InvalidKeyException | StorageException e) {
            return Mono.error(new RuntimeException("Unable to parse credentials or container name.", e));
        } catch (URISyntaxException e) {
            return Mono.error(new RuntimeException("Unable to create container: " + containerName, e));
        }

        return Mono.usingWhen(
            Mono.fromCompletionStage(testHelper.createEventHubClientAsync()),
            client -> Mono.fromCompletionStage(client.getRuntimeInformation())
                .flatMapMany(runtimeInformation -> {
                    for (String id : runtimeInformation.getPartitionIds()) {
                        partitionProcessorMap.put(id, new SamplePartitionProcessor(() -> eventRaised()));
                    }
                    return Flux.fromArray(runtimeInformation.getPartitionIds());
                })
                .flatMap(partitionId -> {
                    if (options.publishMessages()) {
                        return testHelper.sendMessages(client, partitionId, options.getEventsToSend());
                    } else {
                        return Mono.empty();
                    }
                })
                .then(),
            client -> Mono.fromCompletionStage(client.close()));
    }

    @Override
    public Mono<Void> setupAsync() {
        final ConnectionStringBuilder connectionStringBuilder = testHelper.getConnectionStringBuilder();
        final EventProcessorHost.EventProcessorHostBuilder.OptionalStep builder =
            EventProcessorHost.EventProcessorHostBuilder.newBuilder(
                    connectionStringBuilder.getEndpoint().toString(), options.getConsumerGroup())
                .useAzureStorageCheckpointLeaseManager(storageCredentials, containerName, STORAGE_PREFIX)
                .useEventHubConnectionString(connectionStringBuilder.toString())
                .setExecutor(testHelper.getScheduler());

        this.processor = builder.build();

        return Mono.fromCompletionStage(this.processor.registerEventProcessorFactory(processorFactory));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        if (processor == null) {
            return Mono.empty();
        }

        return Mono.fromCompletionStage(processor.unregisterEventProcessor());
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        System.out.println("Cleaning up.");

        CloudBlockBlob blob = null;
        try {
            blob = containerReference.getBlockBlobReference("results.txt");
        } catch (URISyntaxException | StorageException e) {
            System.err.println("Could not create block blob reference to write results.txt. " + e);
        }

        if (blob == null) {
            System.out.println(HEADERS);
            outputPartitionResults(System.out::println);
            return super.cleanupAsync();
        }

        try (BlobOutputStream blobOutputStream = blob.openOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(blobOutputStream, StandardCharsets.UTF_8)) {

            outputPartitionResults(content -> {
                System.out.println(content);

                try {
                    writer.write(content);
                } catch (IOException e) {
                    System.err.printf("Unable to write %s. Error: %s%n", content, e);
                }
            });

        } catch (StorageException | IOException e) {
            System.err.println("Unable to create or write to blob. Error: " + e);
        }

        System.out.println("Done.");

        return Mono.empty();
    }

    private void outputPartitionResults(Consumer<String> onOutput) {
        onOutput.accept(HEADERS);

        for (SamplePartitionProcessor processor : partitionProcessorMap.values()) {
            processor.onStop();
            final List<EventsCounter> counters = processor.getCounters();
            for (int i = 0; i < counters.size(); i++) {
                final EventsCounter eventsCounter = counters.get(i);
                final String result = getResults(i, eventsCounter);
                onOutput.accept(result);
            }
        }

        onOutput.accept("");
        onOutput.accept(String.format("Total Events\t%d%n", getCompletedOperations()));
    }

    private static String getResults(int index, EventsCounter eventsCounter) {
        final double seconds = eventsCounter.elapsedTime() * 0.000000001;
        final double operationsSecond = eventsCounter.totalEvents() / seconds;

        return String.format(FORMAT_STRING, eventsCounter.getPartitionId(), index,
            eventsCounter.totalEvents(), eventsCounter.elapsedTime(), seconds, operationsSecond);
    }
}
