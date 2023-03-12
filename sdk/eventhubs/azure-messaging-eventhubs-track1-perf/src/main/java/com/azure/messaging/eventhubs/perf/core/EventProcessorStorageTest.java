package com.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.messaging.eventhubs.perf.Main;
import com.azure.messaging.eventhubs.perf.SampleEventProcessorFactory;
import com.azure.messaging.eventhubs.perf.SamplePartitionProcessor;
import com.azure.perf.test.core.EventPerfTest;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventprocessorhost.*;
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
        eventHubName = "";

        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalStateException("Environment variable EVENTHUBS_CONNECTION_STRING must be set");
        }

        if (CoreUtils.isNullOrEmpty(eventHubName)) {
            throw new IllegalStateException("Environment variable EVENTHUB_NAME must be set");
        }

        eventDataBytes = Util.generateString(100).getBytes(StandardCharsets.UTF_8);

        StorageCredentials storageCredentials = null;
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
                EventProcessor.eventPerfTest = this;
                EventProcessorOptions options = new EventProcessorOptions();
                options.setPrefetchCount(7999);
                options.setMaxBatchSize(7998);
                eventProcessorHost.registerEventProcessor(EventProcessor.class, options);
                return Mono.empty();
            }));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return super.cleanupAsync().then(Mono.defer(() -> {
            eventProcessorHost.unregisterEventProcessor();
            System.out.println("Cleanup: unregister event processor.");
            scheduler.shutdown();
            return Mono.empty();
        }));
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(Mono.defer(() -> Util.preLoadEvents(eventHubClient, options.getPartitionId() != null ? String.valueOf(options.getPartitionId()) : null , eventDataBytes, options.getEvents())));
    }


    public static class EventProcessor implements IEventProcessor {
        public static EventPerfTest<EventHubsPerfStressOptions> eventPerfTest;
        private int checkpointBatchingCount = 0;

        // OnOpen is called when a new event processor instance is created by the host. In a real implementation, this
        // is the place to do initialization so that events can be processed when they arrive, such as opening a database
        // connection.
        @Override
        public void onOpen(PartitionContext context) throws Exception {
            System.out.println("SAMPLE: Partition " + context.getPartitionId() + " is opening");
        }

        // OnClose is called when an event processor instance is being shut down. The reason argument indicates whether the shut down
        // is because another host has stolen the lease for this partition or due to error or host shutdown. In a real implementation,
        // this is the place to do cleanup for resources that were opened in onOpen.
        @Override
        public void onClose(PartitionContext context, CloseReason reason) throws Exception {
            System.out.println("SAMPLE: Partition " + context.getPartitionId() + " is closing for reason " + reason.toString());
        }

        // onError is called when an error occurs in EventProcessorHost code that is tied to this partition, such as a receiver failure.
        // It is NOT called for exceptions thrown out of onOpen/onClose/onEvents. EventProcessorHost is responsible for recovering from
        // the error, if possible, or shutting the event processor down if not, in which case there will be a call to onClose. The
        // notification provided to onError is primarily informational.
        @Override
        public void onError(PartitionContext context, Throwable error) {
            System.out.println("SAMPLE: Partition " + context.getPartitionId() + " onError: " + error.toString());
        }

        // onEvents is called when events are received on this partition of the Event Hub. The maximum number of events in a batch
        // can be controlled via EventProcessorOptions. Also, if the "invoke processor after receive timeout" option is set to true,
        // this method will be called with null when a receive timeout occurs.
        @Override
        public void onEvents(PartitionContext context, Iterable<EventData> events) throws Exception {
            int eventCount = 0;
            for (EventData data : events)
            {
                eventPerfTest.eventRaised();
            }
//            System.out.println("SAMPLE: Partition " + context.getPartitionId() + " batch size was " + eventCount + " for host " + context.getOwner());
        }
    }
}
