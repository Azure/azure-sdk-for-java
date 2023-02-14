package com.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.checkpointstore.jedis.JedisRedisCheckpointStore;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.perf.EventProcessorJedisOptions;
import com.azure.perf.test.core.EventPerfTest;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class EventProcessorStorageTest extends EventPerfTest<EventProcessorJedisOptions> {

    private final EventProcessorClient eventProcessorClient;
    protected static final String CONTAINER_NAME = "perfstress-" + UUID.randomUUID();
    protected String connectionString;
    protected String eventhubsConnectionString;
    protected String eventHubName;
    protected BlobContainerAsyncClient containerAsyncClient;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public EventProcessorStorageTest(EventProcessorJedisOptions options) {
        super(options);
        Duration errorAfter = options.getErrorAfterInSeconds() > 0
            ? Duration.ofSeconds(options.getErrorAfterInSeconds()) : null;

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(poolConfig, options.getHostName(), 6380, 5000, 1000, options.getPassword(), Protocol.DEFAULT_DATABASE, options.getUserName(), true, null, null, null);
        JedisRedisCheckpointStore checkpointStore = new JedisRedisCheckpointStore(jedisPool);

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

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(containerAsyncClient);

        Consumer<ErrorContext> errorProcessor = errorContext -> super.errorRaised(errorContext.getThrowable());
        Consumer<EventContext> eventProcessor = eventContext -> {
            super.eventRaised();
            eventContext.updateCheckpoint();
        };

        Map<String, EventPosition> initalPositionMap = new HashMap<>();
        for (int i = 0; i < options.getPartitions(); i++) {
            initalPositionMap.put(String.valueOf(i), EventPosition.earliest());
        }

        eventProcessorClient = new EventProcessorClientBuilder()
            .connectionString(options.getConnectionString(), options.getEventHubName())
            .consumerGroup(options.getConsumerGroup())
            .checkpointStore(blobCheckpointStore)
            .processError(errorProcessor)
            .processEvent(eventProcessor)
            .initialPartitionEventPosition(initalPositionMap)
            .buildEventProcessorClient();
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(Mono.defer(() -> {
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
}
