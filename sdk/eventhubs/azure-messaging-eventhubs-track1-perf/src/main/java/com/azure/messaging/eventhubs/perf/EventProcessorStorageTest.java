package com.azure.messaging.eventhubs.perf;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.messaging.eventhubs.perf.SampleEventProcessorFactory;
import com.azure.messaging.eventhubs.perf.SamplePartitionProcessor;
import com.azure.messaging.eventhubs.perf.core.EventHubsPerfStressOptions;
import com.azure.messaging.eventhubs.perf.core.Util;
import com.azure.perf.test.core.EventPerfTest;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.eventprocessorhost.EventProcessorOptions;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class EventProcessorStorageTest extends EventPerfTest<EventHubsPerfStressOptions> {

    protected static final String CONTAINER_NAME = "perfstress-" + UUID.randomUUID();
    public static final String FORMAT_BASE_URI = "https://%s.blob.core.windows.net";
    private final ScheduledExecutorService scheduler;
    private ConcurrentHashMap<String, SamplePartitionProcessor> partitionProcessorMap;
    private SampleEventProcessorFactory processorFactory;
    private EventProcessorHost eventProcessorHost;
    protected String connectionString;
    protected String eventhubsConnectionString;
    protected String eventHubName;
    protected byte[] eventDataBytes;
    protected EventHubClient eventHubClient;
    private final EventProcessorOptions eventProcessorOptions;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public EventProcessorStorageTest(EventHubsPerfStressOptions options) {
        super(options);
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        connectionString = configuration.get("STORAGE_CONNECTION_STRING");

        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new RuntimeException("Storage Connection String cannot be null.");
        }

        eventhubsConnectionString = System.getenv("EVENTHUBS_CONNECTION_STRING");
        eventHubName = System.getenv("EVENTHUB_NAME");

        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalStateException("Environment variable EVENTHUBS_CONNECTION_STRING must be set");
        }

        if (CoreUtils.isNullOrEmpty(eventHubName)) {
            throw new IllegalStateException("Environment variable EVENTHUB_NAME must be set");
        }

        eventDataBytes = Util.generateString(options.getMessageSize()).getBytes(StandardCharsets.UTF_8);

        StorageCredentials storageCredentials;
        try {
            storageCredentials = StorageCredentials.tryParseCredentials(connectionString);
        } catch (InvalidKeyException | StorageException e) {
            throw new RuntimeException(e);
        }

        final StorageUri storageUri = new StorageUri(URI.create(String.format(FORMAT_BASE_URI,
            storageCredentials.getAccountName())));
        final CloudBlobClient client = new CloudBlobClient(storageUri, storageCredentials);

        try {
            CloudBlobContainer containerReference = client.getContainerReference(CONTAINER_NAME);
            if (containerReference.deleteIfExists()) {
                System.out.printf("Deleting %s because it existed before.%n", CONTAINER_NAME);
            }
            containerReference.create();
        } catch (StorageException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        partitionProcessorMap = new ConcurrentHashMap<>();
        processorFactory = new SampleEventProcessorFactory(partitionProcessorMap);

        final ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder(eventhubsConnectionString)
            .setEventHubName(eventHubName);

        this.scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 4);

        final EventProcessorHost.EventProcessorHostBuilder.OptionalStep builder =
            EventProcessorHost.EventProcessorHostBuilder.newBuilder(
                    connectionStringBuilder.getEndpoint().toString(), options.getConsumerGroup())
                .useAzureStorageCheckpointLeaseManager(storageCredentials, CONTAINER_NAME, "perf")
                .useEventHubConnectionString(connectionStringBuilder.toString())
                .setExecutor(scheduler);

        eventProcessorHost = builder.build();
        eventProcessorOptions = new EventProcessorOptions();
        eventProcessorOptions.setPrefetchCount(options.getPrefetch());
        eventProcessorOptions.setMaxBatchSize(options.getBatchSize());

        try {
            eventHubClient = EventHubClient
                .createFromConnectionStringSync(connectionStringBuilder.toString(), scheduler);
        } catch (IOException | EventHubException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(Mono.defer(() -> Mono.fromFuture(eventHubClient.getRuntimeInformation())
                .map(eventHubRuntimeInformation -> {
                    for (String id : eventHubRuntimeInformation.getPartitionIds()) {
                        partitionProcessorMap.put(id, new SamplePartitionProcessor(this));
                    }
                    return Mono.empty();
                })))
            .then(Mono.defer(() -> {
                eventProcessorHost.registerEventProcessorFactory(processorFactory, eventProcessorOptions);
                return Mono.empty();
            }));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return super.cleanupAsync().then(Mono.defer(() -> {
            eventProcessorHost.unregisterEventProcessor();
            scheduler.shutdown();
            return Mono.empty();
        }));
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(Mono.defer(() -> Util.preLoadEvents(eventHubClient, options.getPartitionId() != null
                ? String.valueOf(options.getPartitionId()) : null , eventDataBytes, options.getEvents())));
    }
}
