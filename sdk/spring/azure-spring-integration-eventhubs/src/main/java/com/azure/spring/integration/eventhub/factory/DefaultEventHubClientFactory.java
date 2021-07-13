// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.factory;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.ClientOptions;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.spring.core.util.Memoizer;
import com.azure.spring.core.util.Tuple;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.impl.EventHubProcessor;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.spring.core.ApplicationId.AZURE_SPRING_EVENT_HUB;
import static com.azure.spring.core.ApplicationId.VERSION;

/**
 * Default implementation of {@link EventHubClientFactory}.
 *
 * @author Warren Zhu
 * @author Xiaolu Dai
 */
public class DefaultEventHubClientFactory implements EventHubClientFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventHubClientFactory.class);

    // Maps used for cache and clean up clients
    // (eventHubName, consumerGroup) -> consumerClient
    private final Map<Tuple<String, String>, EventHubConsumerAsyncClient> consumerClientMap = new ConcurrentHashMap<>();
    // eventHubName -> producerClient
    private final Map<String, EventHubProducerAsyncClient> producerClientMap = new ConcurrentHashMap<>();
    // (eventHubName, consumerGroup) -> eventProcessorClient
    private final Map<Tuple<String, String>, EventProcessorClient> processorClientMap = new ConcurrentHashMap<>();
    private final String checkpointStorageConnectionString;
    private final String checkpointStorageContainer;
    private final String eventHubConnectionString;
    // Memoized functional client creator
    private final BiFunction<String, String, EventHubConsumerAsyncClient> eventHubConsumerClientCreator =
        Memoizer.memoize(consumerClientMap, this::createEventHubClient);
    private final Function<String, EventHubProducerAsyncClient> producerClientCreator =
        Memoizer.memoize(producerClientMap, this::createProducerClient);

    public DefaultEventHubClientFactory(@NonNull String eventHubConnectionString,
                                        String checkpointConnectionString,
                                        String checkpointStorageContainer) {
        Assert.hasText(checkpointConnectionString, "checkpointConnectionString can't be null or empty");
        this.eventHubConnectionString = eventHubConnectionString;
        this.checkpointStorageConnectionString = checkpointConnectionString;
        this.checkpointStorageContainer = checkpointStorageContainer;
    }

    private EventHubConsumerAsyncClient createEventHubClient(String eventHubName, String consumerGroup) {
        return new EventHubClientBuilder()
            .connectionString(eventHubConnectionString, eventHubName)
            .consumerGroup(consumerGroup)
            .clientOptions(new ClientOptions().setApplicationId(AZURE_SPRING_EVENT_HUB + VERSION))
            .buildAsyncConsumerClient();
    }

    private EventHubProducerAsyncClient createProducerClient(String eventHubName) {
        return new EventHubClientBuilder()
            .connectionString(eventHubConnectionString, eventHubName)
            .clientOptions(new ClientOptions().setApplicationId(AZURE_SPRING_EVENT_HUB + VERSION))
            .buildAsyncProducerClient();
    }

    private EventProcessorClient createEventProcessorClientInternal(String eventHubName, String consumerGroup,
                                                                    EventHubProcessor eventHubProcessor) {

        // We set eventHubName as the container name when we use track1 library, and the EventHubProcessor will create
        // the container automatically if not exists
        String containerName = checkpointStorageContainer == null ? eventHubName : checkpointStorageContainer;

        BlobContainerAsyncClient blobClient = new BlobContainerClientBuilder()
            .connectionString(checkpointStorageConnectionString)
            .containerName(containerName)
            .httpLogOptions(new HttpLogOptions().setApplicationId(AZURE_SPRING_EVENT_HUB + VERSION))
            .buildAsyncClient();

        final Boolean isContainerExist = blobClient.exists().block();
        if (isContainerExist == null || !isContainerExist) {
            LOGGER.warn("Will create storage blob {}, the auto creation might be deprecated in later versions.",
                containerName);
            blobClient.create().block(Duration.ofMinutes(5L));
        }

        // TODO (xiada): set up event processing position for each partition
        return new EventProcessorClientBuilder()
            .connectionString(eventHubConnectionString, eventHubName)
            .consumerGroup(consumerGroup)
            .checkpointStore(new BlobCheckpointStore(blobClient))
            .processPartitionInitialization(eventHubProcessor::onInitialize)
            .processPartitionClose(eventHubProcessor::onClose)
            .processEvent(eventHubProcessor::onEvent)
            .processError(eventHubProcessor::onError)
            .buildEventProcessorClient();
    }

    private <K, V> void close(Map<K, V> map, Consumer<V> close) {
        map.values().forEach(it -> {
            try {
                close.accept(it);
            } catch (Exception ex) {
                LOGGER.warn("Failed to clean event hub client factory", ex);
            }
        });
    }

    @Override
    public void destroy() {
        close(consumerClientMap, EventHubConsumerAsyncClient::close);
        close(producerClientMap, EventHubProducerAsyncClient::close);
        close(processorClientMap, EventProcessorClient::stop);
    }

    @Override
    public EventHubConsumerAsyncClient getOrCreateConsumerClient(String eventHubName, String consumerGroup) {
        return this.eventHubConsumerClientCreator.apply(eventHubName, consumerGroup);
    }

    @Override
    public EventHubProducerAsyncClient getOrCreateProducerClient(String eventHubName) {
        return this.producerClientCreator.apply(eventHubName);
    }

    @Override
    public EventProcessorClient createEventProcessorClient(String eventHubName, String consumerGroup,
                                                           EventHubProcessor processor) {
        return processorClientMap.computeIfAbsent(Tuple.of(eventHubName, consumerGroup), (t) ->
            createEventProcessorClientInternal(eventHubName, consumerGroup, processor));
    }

    @Override
    public Optional<EventProcessorClient> getEventProcessorClient(String eventHubName, String consumerGroup) {
        return Optional.ofNullable(this.processorClientMap.get(Tuple.of(eventHubName, consumerGroup)));
    }

    @Override
    public EventProcessorClient removeEventProcessorClient(String eventHubName, String consumerGroup) {
        return this.processorClientMap.remove(Tuple.of(eventHubName, consumerGroup));
    }
}
