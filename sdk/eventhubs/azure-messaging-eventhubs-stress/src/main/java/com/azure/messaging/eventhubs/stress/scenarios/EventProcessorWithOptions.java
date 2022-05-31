// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.stress.util.Constants;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Process events with options from application arguments. <br/>
 * Required options: <br/>
 * --UPDATE_CHECKPOINT=YES/NO <br/>
 * --NEED_SEND_EVENT_HUB=YES/NO <br/>
 */
@Service("EventProcessorWithOptions")
public class EventProcessorWithOptions extends EventHubsScenario {
    private static final Logger logger = LoggerFactory.getLogger(EventProcessorWithOptions.class);

    private static final int PARTITION_NUMBER = 64;
    private static final int EVENT_COUNT_THRESHOLD = 40;
    private static final int ONE_SECOND_IN_NANO = 1000000000;

    @Override
    public void run() {
        final String storageConnStr = options.get(Constants.STORAGE_CONNECTION_STRING);
        final String containerName = options.get(Constants.STORAGE_CONTAINER_NAME);
        final String eventHubConnStr = options.get(Constants.EVENTHUBS_CONNECTION_STRING);
        final String eventHub = options.get(Constants.EVENTHUBS_EVENT_HUB_NAME);
        final String consumerGroup = options.get(Constants.EVENTHUBS_CONSUMER_GROUP, EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME);

        Objects.requireNonNull(options.get(Constants.UPDATE_CHECKPOINT), "UPDATE_CHECKPOINT argument required. value should be YES or NO");
        Objects.requireNonNull(options.get(Constants.NEED_SEND_EVENT_HUB), "NEED_SEND_EVENT_HUB argument required. value should be YES or NO");
        final boolean updateCheckpoint = "YES".equalsIgnoreCase(options.get(Constants.UPDATE_CHECKPOINT));
        final boolean needSendEventHub = "YES".equalsIgnoreCase(options.get(Constants.NEED_SEND_EVENT_HUB));
        final String writeEventHubConnStr = options.get(Constants.SECOND_EVENTHUBS_CONNECTION_STRING);
        final String writeEventHub = options.get(Constants.SECOND_EVENTHUBS_EVENT_HUB_NAME);

        EventHubProducerClient producerClient = new EventHubClientBuilder()
            .connectionString(writeEventHubConnStr, writeEventHub)
            .buildProducerClient();

        final String batchSize = options.get(Constants.RECEIVE_BATCH_SIZE);
        final String batchTimeoutMs = options.get(Constants.RECEIVE_BATCH_TIMEOUT);

        final int[] eventCounter = new int[PARTITION_NUMBER];
        final List<ArrayList<EventData>> eventsFromPartitions = new ArrayList<>(PARTITION_NUMBER);
        for (int i = 0; i < PARTITION_NUMBER; i++) {
            eventsFromPartitions.add(new ArrayList<>(EVENT_COUNT_THRESHOLD));
        }

        Consumer<EventContext> processEvent = eventContext -> {
            logEvent("Processing", eventContext);

            if (updateCheckpoint) {
                final int partitionIndex = Integer.parseInt(eventContext.getPartitionContext().getPartitionId());
                eventsFromPartitions.get(partitionIndex).add(new EventData(eventContext.getEventData().getBody()));
                eventCounter[partitionIndex]++;
                if (eventCounter[partitionIndex] >= EVENT_COUNT_THRESHOLD) {
                    eventCounter[partitionIndex] = 0;
                    try {
                        long startTime = System.nanoTime();
                        eventContext.updateCheckpointAsync().block(Duration.ofSeconds(3));
                        long endTime = System.nanoTime();
                        if (needSendEventHub) {
                            producerClient.send(eventsFromPartitions.get(partitionIndex));
                        }

                        if (endTime - startTime > ONE_SECOND_IN_NANO) {
                            trackEvent(eventContext, startTime, endTime);
                        }
                    } catch (Throwable t) {
                        logEventError(eventContext, t);
                    }
                }
            }

            trackMetric(eventContext);
            logEvent("Processed", eventContext);
        };

        Consumer<EventBatchContext> processEventBatch = eventContext -> {
            if (eventContext.getEvents().size() > 0) {
                logEvent("Processing", eventContext);

                if (updateCheckpoint) {
                    eventContext.updateCheckpoint();
                }

                trackMetric(eventContext);
                logEvent("Processed", eventContext);
            }
        };

        Consumer<ErrorContext> processError = errorContext -> {
            logger.error("Error while processing {}, {}, {}, {}", errorContext.getPartitionContext().getEventHubName(),
                errorContext.getPartitionContext().getConsumerGroup(),
                errorContext.getPartitionContext().getPartitionId(),
                ExceptionUtils.getStackTrace(errorContext.getThrowable()));
            telemetryClient.trackException(new Exception(errorContext.getThrowable()), new HashMap<String, String>() {{
                put("EventHub", errorContext.getPartitionContext().getEventHubName());
                put("ConsumerGroup", errorContext.getPartitionContext().getConsumerGroup());
                put("Partition", errorContext.getPartitionContext().getPartitionId());
                put("ErrorTime", Instant.now().toString());
            }}, null);
        };

        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
            .connectionString(storageConnStr)
            .containerName(containerName)
            .buildAsyncClient();

        BlobCheckpointStore checkpointStore = new BlobCheckpointStore(blobContainerAsyncClient);

        Map<String, EventPosition> initialPositions = new HashMap<>();
        final int partitionCount = this.getNumberOfPartitions(eventHubConnStr, eventHub);
        for (int i = 0; i < partitionCount; i++) {
            initialPositions.put(String.valueOf(i), EventPosition.earliest());
        }

        final EventProcessorClientBuilder eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .consumerGroup(consumerGroup)
            .connectionString(eventHubConnStr, eventHub)
            .loadBalancingStrategy(LoadBalancingStrategy.GREEDY)
            .initialPartitionEventPosition(initialPositions)
            .processError(processError)
            .checkpointStore(checkpointStore);
        if (batchSize != null && batchTimeoutMs != null) {
            eventProcessorClientBuilder.processEventBatch(processEventBatch, Integer.parseInt(batchSize),
                Duration.ofMillis(Integer.parseInt(batchTimeoutMs)));
        } else if (batchSize != null) {
            eventProcessorClientBuilder.processEventBatch(processEventBatch, Integer.parseInt(batchSize));
        } else {
            eventProcessorClientBuilder.processEvent(processEvent);
        }
        EventProcessorClient eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();
        eventProcessorClient.start();
    }

    private void logEvent(String status, EventContext eventContext) {
        logger.debug(
            status + " event: Event Hub name = {}; consumer group name = {}; partition id = {}; sequence number = {}; offset = {}, enqueued time = {}",
            eventContext.getPartitionContext().getEventHubName(),
            eventContext.getPartitionContext().getConsumerGroup(),
            eventContext.getPartitionContext().getPartitionId(),
            eventContext.getEventData().getSequenceNumber(),
            eventContext.getEventData().getOffset(),
            eventContext.getEventData().getEnqueuedTime()
        );
    }

    private void logEvent(String status, EventBatchContext eventContext) {
        logger.debug(
            status + " event: Event Hub name = {}; consumer group name = {}; partition id = {}; last sequence number = {}, batch size = {}",
            eventContext.getPartitionContext().getEventHubName(),
            eventContext.getPartitionContext().getConsumerGroup(),
            eventContext.getPartitionContext().getPartitionId(),
            eventContext.getEvents().get(eventContext.getEvents().size() - 1).getSequenceNumber(),
            eventContext.getEvents().size());
    }

    private void logEventError(EventContext eventContext, Throwable t) {
        logger.debug(
            "Checkpoint update failed: Event Hub name = {}; consumer group name = {}; partition id = {}; sequence number = {}; offset = {}, enqueued time = {}, error = {}",
            eventContext.getPartitionContext().getEventHubName(),
            eventContext.getPartitionContext().getConsumerGroup(),
            eventContext.getPartitionContext().getPartitionId(),
            eventContext.getEventData().getSequenceNumber(),
            eventContext.getEventData().getOffset(),
            eventContext.getEventData().getEnqueuedTime(),
            ExceptionUtils.getStackTrace(t));
    }

    private void trackEvent(EventContext eventContext, long startTime, long endTime) {
        telemetryClient.trackEvent("updateCheckpoint takes long", new HashMap<String, String>() {{
            put("Namespace", eventContext.getPartitionContext().getFullyQualifiedNamespace());
            put("EventHub", eventContext.getPartitionContext().getEventHubName());
            put("ConsumerGroup", eventContext.getPartitionContext().getConsumerGroup());
            put("Partition", eventContext.getPartitionContext().getPartitionId());
            put("TimeElapsed", String.valueOf(endTime - startTime));
        }}, null);
    }

    private void trackMetric(EventContext eventContext) {
        String metricKey = String.format("%s/%s/%s/%s",
            eventContext.getPartitionContext().getFullyQualifiedNamespace(),
            eventContext.getPartitionContext().getEventHubName(),
            eventContext.getPartitionContext().getConsumerGroup(),
            eventContext.getPartitionContext().getPartitionId()
        );
        rateMeter.add(metricKey, 1);
    }

    private void trackMetric(EventBatchContext eventContext) {
        String metricKey = String.format("%s/%s/%s/%s",
            eventContext.getPartitionContext().getFullyQualifiedNamespace(),
            eventContext.getPartitionContext().getEventHubName(),
            eventContext.getPartitionContext().getConsumerGroup(),
            eventContext.getPartitionContext().getPartitionId()
        );
        rateMeter.add(metricKey, 1);
    }

    private int getNumberOfPartitions(String connectionString, String eventHub) {
        try (EventHubConsumerClient consumerClient = new EventHubClientBuilder()
            .connectionString(connectionString, eventHub)
            .consumerGroup("$default")
            .buildConsumerClient()) {
            return (int) consumerClient.getEventHubProperties().getPartitionIds().stream().count();
        }
    }

}
