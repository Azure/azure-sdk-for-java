package com.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.perf.test.core.EventPerfTest;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.azure.messaging.eventhubs.perf.core.Util.generateString;

public class EventProcessorBatchStorageTest extends EventPerfTest<EventHubsPerfOptions> {

    private final EventProcessorClient eventProcessorClient;
    protected static final String CONTAINER_NAME = "perfstress-" + UUID.randomUUID();
    protected String connectionString;
    protected String eventhubsConnectionString;
    protected BlobContainerAsyncClient containerAsyncClient;
    protected EventHubClientBuilder eventHubClientBuilder;
    protected EventHubProducerAsyncClient eventHubProducerAsyncClient;
    protected EventHubProducerClient eventHubProducerClient;
    protected String eventHubName;
    protected byte[] eventDataBytes;


    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public EventProcessorBatchStorageTest(EventHubsPerfOptions options) {
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
        containerAsyncClient = new BlobContainerClientBuilder()
            .connectionString(connectionString)
            .containerName(CONTAINER_NAME)
            .buildAsyncClient();

        containerAsyncClient.createIfNotExists().block();

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(containerAsyncClient);

        Consumer<ErrorContext> errorProcessor = errorContext -> {System.out.println("err + " + errorContext.getThrowable().getMessage());};

        Map<String, EventPosition> initalPositionMap = new HashMap<>();
        for (int i = 0; i < 32; i++) {
            initalPositionMap.put(String.valueOf(i), EventPosition.earliest());
        }

        eventHubClientBuilder = new EventHubClientBuilder().connectionString(eventhubsConnectionString, eventHubName);
        eventHubProducerAsyncClient = eventHubClientBuilder.buildAsyncProducerClient();
        eventHubProducerClient = eventHubClientBuilder.buildProducerClient();

        eventDataBytes = generateString(100).getBytes(StandardCharsets.UTF_8);

        eventProcessorClient = new EventProcessorClientBuilder()
            .connectionString(eventhubsConnectionString, eventHubName)
            .consumerGroup(options.getConsumerGroup())
            .checkpointStore(blobCheckpointStore)
            .processError(errorProcessor)
            .loadBalancingStrategy(LoadBalancingStrategy.GREEDY)
            .processEventBatch(eventBatchContext -> {
                for (EventData eventData : eventBatchContext.getEvents()) {
                    super.eventRaised();
                }
            }, options.getCount())
            .initialPartitionEventPosition(initalPositionMap)
            .prefetchCount(options.getPrefetch())
            .buildEventProcessorClient();
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(Mono.defer(() -> {
                eventProcessorClient.start();
                return Mono.empty();
            }));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return super.cleanupAsync().then(Mono.defer(() -> {
            eventProcessorClient.stop();
            System.out.println("Began cleanup");
            return Mono.empty();
        }));
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(Mono.defer(() -> Util.preLoadEvents(eventHubProducerAsyncClient, options.getPartitionId() != null
                ? String.valueOf(options.getPartitionId()) : null , options.getEvents(), eventDataBytes)));
    }
}
