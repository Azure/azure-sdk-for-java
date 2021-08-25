package com.azure.spring.integration.eventhub.factory;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.spring.cloud.context.core.util.Tuple;
import com.azure.spring.integration.eventhub.api.ProcessorConsumerFactory;
import com.azure.spring.integration.eventhub.impl.EventHubProcessor;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.spring.cloud.context.core.util.Constants.SPRING_EVENT_HUB_APPLICATION_ID;

public class DefaultEventHubProcessorConsumerClientFactory implements ProcessorConsumerFactory, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventHubProcessorConsumerClientFactory.class);

    private final Map<Tuple<String, String>, EventProcessorClient> processorClientMap = new ConcurrentHashMap<>();

    private final String checkpointStorageConnectionString;

    private final String checkpointStorageContainer;

    private final String eventHubConnectionString;

    public DefaultEventHubProcessorConsumerClientFactory(@NonNull String eventHubConnectionString,
                                        String checkpointConnectionString,
                                        String checkpointStorageContainer) {
        this.eventHubConnectionString = eventHubConnectionString;
        this.checkpointStorageConnectionString = checkpointConnectionString;
        this.checkpointStorageContainer = checkpointStorageContainer;
    }

    @Override
    public void destroy() {
        processorClientMap.values().forEach(processorClient -> {
            try {
                processorClient.stop();
            } catch (Exception ex) {
                LOGGER.warn("Failed to clean event hub client factory", ex);
            }
        });
    }


    private EventProcessorClient createEventProcessorClientInternal(String eventHubName, String consumerGroup,
                                                                    EventHubProcessor eventHubProcessor) {
        // We set eventHubName as the container name when we use track1 library, and the EventHubProcessor will create
        // the container automatically if not exists
        String containerName = checkpointStorageContainer == null ? eventHubName : checkpointStorageContainer;

        BlobContainerAsyncClient blobClient = new BlobContainerClientBuilder()
            .connectionString(checkpointStorageConnectionString)
            .containerName(containerName)
            .httpLogOptions(new HttpLogOptions().setApplicationId(SPRING_EVENT_HUB_APPLICATION_ID))
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
