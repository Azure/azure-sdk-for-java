// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
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
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Process events with options from application arguments.
 * <p>
 * Support options:
 * <pre>
 * --NEED_UPDATE_CHECKPOINT boolean
 * --UPDATE_CHECKPOINT_TIMEOUT_IN_SECONDS int
 * --NEED_SEND_EVENT_HUB boolean
 * --MAX_BATCH_SIZE  int
 * --BATCH_TIMEOUT_IN_MILLIS int
 * </pre>
 */
@Component("EventProcessorWithOptions")
public class EventProcessorWithOptions extends EventHubsScenario {
    private static final ClientLogger LOGGER = new ClientLogger(EventProcessorWithOptions.class);

    // The max number of partitions that the producer client will send to
    private static final int MAX_PARTITION_NUMBER = 64;
    // The number of events to update checkpoint each time
    private static final int UPDATE_CHECKPOINT_EVENT_NUMBER = 40;
    // If the time exceeds the required threshold, then send event to application insights
    private static final int UPDATE_TIME_THRESHOLD_IN_MILLIS = 1000;
    private static final int SEND_TIME_THRESHOLD_IN_MILLIS = 5000;

    @Value("${NEED_UPDATE_CHECKPOINT:false}")
    private boolean needUpdateCheckpoint;

    @Value("${UPDATE_CHECKPOINT_TIMEOUT_IN_SECONDS:3}")
    private int updateTimeoutInSeconds;

    @Value("${NEED_SEND_EVENT_HUB:false}")
    private boolean needSendEventHub;

    @Value("${MAX_BATCH_SIZE:0}")
    private int batchSize;

    @Value("${BATCH_TIMEOUT_IN_MILLIS:0}")
    private int batchTimeoutInMs;

    @Override
    public void run() {
        final String storageConnStr = options.getStorageConnectionString();
        final String containerName = options.getStorageContainerName();
        final String eventHubConnStr = options.getEventhubsConnectionString();
        final String eventHub = options.getEventhubsEventHubName();
        final String consumerGroup = options.getEventHubsConsumerGroup();
        final String writeEventHub = options.getSecondEventhubsEventHubName();

        // Config producer
        EventHubProducerClient producerClient = new EventHubClientBuilder()
            .connectionString(eventHubConnStr, writeEventHub)
            .buildProducerClient();

        final int[] eventCounter = new int[MAX_PARTITION_NUMBER];
        final List<ArrayList<EventData>> eventsFromPartitions = new ArrayList<>(MAX_PARTITION_NUMBER);
        for (int i = 0; i < MAX_PARTITION_NUMBER; i++) {
            eventsFromPartitions.add(new ArrayList<>(UPDATE_CHECKPOINT_EVENT_NUMBER));
        }

        // Config processEvent function
        Consumer<EventContext> processEvent = eventContext -> {
            logProcessEventStatus(ProcessStatus.PROCESSING, eventContext);

            if (needUpdateCheckpoint) {
                final int partitionIndex = Integer.parseInt(eventContext.getPartitionContext().getPartitionId());

                eventCounter[partitionIndex]++;
                if (eventCounter[partitionIndex] >= UPDATE_CHECKPOINT_EVENT_NUMBER) {
                    eventCounter[partitionIndex] = 0;
                    try {
                        long startTime = System.currentTimeMillis();
                        if (updateTimeoutInSeconds > 0) {
                            eventContext.updateCheckpointAsync().block(Duration.ofSeconds(updateTimeoutInSeconds));
                        } else {
                            eventContext.updateCheckpointAsync().block();
                        }
                        long endTime = System.currentTimeMillis();
                        if (endTime - startTime > UPDATE_TIME_THRESHOLD_IN_MILLIS) {
                            trackExceedTimeThresholdEvent(ProcessStage.UPDATE_CHECKPOINT, eventContext, startTime,
                                endTime);
                        }
                    } catch (Throwable t) {
                        logProcessEventError(ProcessStage.UPDATE_CHECKPOINT, eventContext, t);
                    }
                }

                if (needSendEventHub) {
                    eventsFromPartitions.get(partitionIndex).add(new EventData(eventContext.getEventData().getBody()));
                    try {
                        long startTime = System.currentTimeMillis();
                        producerClient.send(eventsFromPartitions.get(partitionIndex));
                        long endTime = System.currentTimeMillis();
                        if (endTime - startTime > SEND_TIME_THRESHOLD_IN_MILLIS) {
                            trackExceedTimeThresholdEvent(ProcessStage.SEND_EVENT_BATCH, eventContext, startTime,
                                endTime);
                        }
                    } catch (Throwable t) {
                        logProcessEventError(ProcessStage.SEND_EVENT_BATCH, eventContext, t);
                    }
                    eventsFromPartitions.get(partitionIndex).clear();
                }
            }

            addCountMetric(eventContext);
            logProcessEventStatus(ProcessStatus.PROCESSED, eventContext);
        };

        Consumer<EventBatchContext> processEventBatch = eventContext -> {
            if (eventContext.getEvents().size() > 0) {
                logProcessEventStatus(ProcessStatus.PROCESSING, eventContext);

                try {
                    long startTime = System.currentTimeMillis();
                    if (updateTimeoutInSeconds > 0) {
                        eventContext.updateCheckpointAsync().block(Duration.ofSeconds(updateTimeoutInSeconds));
                    } else {
                        eventContext.updateCheckpointAsync().block();
                    }
                    long endTime = System.currentTimeMillis();
                    if (endTime - startTime > UPDATE_TIME_THRESHOLD_IN_MILLIS) {
                        trackExceedTimeThresholdEvent(ProcessStage.UPDATE_CHECKPOINT, eventContext, startTime, endTime);
                    }
                } catch (Throwable t) {
                    logProcessEventError(ProcessStage.UPDATE_CHECKPOINT, eventContext, t);
                }

                addCountMetric(eventContext);
                logProcessEventStatus(ProcessStatus.PROCESSED, eventContext);
            }
        };

        Consumer<ErrorContext> processError = errorContext -> {
            LOGGER.error("Error while processing {}, {}, {}, {}",
                errorContext.getPartitionContext().getEventHubName(),
                errorContext.getPartitionContext().getConsumerGroup(),
                errorContext.getPartitionContext().getPartitionId(),
                getStackTrace(errorContext.getThrowable()));

            trackProcessErrorException(errorContext);
        };

        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
            .connectionString(storageConnStr)
            .containerName(containerName)
            .buildAsyncClient();

        BlobCheckpointStore checkpointStore = new BlobCheckpointStore(blobContainerAsyncClient);

        Map<String, EventPosition> initialPositions = new HashMap<>();
        final int partitionCount = getNumberOfPartitions(eventHubConnStr, eventHub, consumerGroup);
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
        if (batchSize != 0 && batchTimeoutInMs != 0) {
            eventProcessorClientBuilder.processEventBatch(processEventBatch, batchSize,
                Duration.ofMillis(batchTimeoutInMs));
        } else if (batchSize != 0) {
            eventProcessorClientBuilder.processEventBatch(processEventBatch, batchSize);
        } else {
            eventProcessorClientBuilder.processEvent(processEvent);
        }
        EventProcessorClient eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();
        eventProcessorClient.start();
    }

    private void logProcessEventStatus(ProcessStatus status, EventContext eventContext) {
        LOGGER.verbose(
            status + " event: Event Hub name = {}; consumer group name = {}; "
                + "partition id = {}; sequence number = {}; offset = {}, enqueued time = {}",
            eventContext.getPartitionContext().getEventHubName(),
            eventContext.getPartitionContext().getConsumerGroup(),
            eventContext.getPartitionContext().getPartitionId(),
            eventContext.getEventData().getSequenceNumber(),
            eventContext.getEventData().getOffset(),
            eventContext.getEventData().getEnqueuedTime()
        );
    }

    private void logProcessEventStatus(ProcessStatus status, EventBatchContext eventContext) {
        LOGGER.verbose(
            status + " event: Event Hub name = {}; consumer group name = {};"
                + " partition id = {}; batch size = {}; last sequence number = {}",
            eventContext.getPartitionContext().getEventHubName(),
            eventContext.getPartitionContext().getConsumerGroup(),
            eventContext.getPartitionContext().getPartitionId(),
            eventContext.getEvents().get(eventContext.getEvents().size() - 1).getSequenceNumber(),
            eventContext.getEvents().size());
    }

    private void logProcessEventError(ProcessStage stage, EventContext eventContext, Throwable t) {
        LOGGER.error(
            stage + " failed: Event Hub name = {}; consumer group name = {};"
                + " partition id = {}; sequence number = {}; offset = {}, enqueued time = {}, error = {}",
            eventContext.getPartitionContext().getEventHubName(),
            eventContext.getPartitionContext().getConsumerGroup(),
            eventContext.getPartitionContext().getPartitionId(),
            eventContext.getEventData().getSequenceNumber(),
            eventContext.getEventData().getOffset(),
            eventContext.getEventData().getEnqueuedTime(),
            getStackTrace(t));
    }

    private void logProcessEventError(ProcessStage stage, EventBatchContext eventContext, Throwable t) {
        LOGGER.error(
            stage + "  failed: Event Hub name = {}; consumer group name = {};"
                + " partition id = {}; batch size = {}; last sequence number = {}",
            eventContext.getPartitionContext().getEventHubName(),
            eventContext.getPartitionContext().getConsumerGroup(),
            eventContext.getPartitionContext().getPartitionId(),
            eventContext.getEvents().size(),
            eventContext.getEvents().get(eventContext.getEvents().size() - 1).getSequenceNumber(),
            getStackTrace(t));
    }

    private void trackExceedTimeThresholdEvent(ProcessStage stage, EventContext eventContext, long startTime,
                                               long endTime) {
        Map<String, String> properties = new HashMap<>();
        properties.put("Namespace", eventContext.getPartitionContext().getFullyQualifiedNamespace());
        properties.put("EventHub", eventContext.getPartitionContext().getEventHubName());
        properties.put("ConsumerGroup", eventContext.getPartitionContext().getConsumerGroup());
        properties.put("Partition", eventContext.getPartitionContext().getPartitionId());
        properties.put("TimeElapsed", String.valueOf(endTime - startTime));

        telemetryClient.trackEvent(stage + " takes long", properties, null);
    }

    private void trackExceedTimeThresholdEvent(ProcessStage stage, EventBatchContext eventContext, long startTime,
                                               long endTime) {
        Map<String, String> properties = new HashMap<>();
        properties.put("Namespace", eventContext.getPartitionContext().getFullyQualifiedNamespace());
        properties.put("EventHub", eventContext.getPartitionContext().getEventHubName());
        properties.put("ConsumerGroup", eventContext.getPartitionContext().getConsumerGroup());
        properties.put("Partition", eventContext.getPartitionContext().getPartitionId());
        properties.put("TimeElapsed", String.valueOf(endTime - startTime));

        telemetryClient.trackEvent(stage + " takes long", properties, null);
    }

    private void trackProcessErrorException(ErrorContext errorContext) {
        Map<String, String> properties = new HashMap<>();
        properties.put("EventHub", errorContext.getPartitionContext().getEventHubName());
        properties.put("ConsumerGroup", errorContext.getPartitionContext().getConsumerGroup());
        properties.put("Partition", errorContext.getPartitionContext().getPartitionId());
        properties.put("ErrorTime", Instant.now().toString());

        telemetryClient.trackException(new Exception(errorContext.getThrowable()), properties, null);
    }

    private void addCountMetric(EventContext eventContext) {
        String metricKey = String.format("Number of processed event - %s/%s/%s",
            eventContext.getPartitionContext().getEventHubName(),
            eventContext.getPartitionContext().getConsumerGroup(),
            eventContext.getPartitionContext().getPartitionId()
        );
        rateMeter.add(metricKey, 1);
    }

    private void addCountMetric(EventBatchContext eventContext) {
        String metricKey = String.format("Number of processed event batch - %s/%s/%s",
            eventContext.getPartitionContext().getEventHubName(),
            eventContext.getPartitionContext().getConsumerGroup(),
            eventContext.getPartitionContext().getPartitionId()
        );
        rateMeter.add(metricKey, 1);
    }

    private int getNumberOfPartitions(String connectionString, String eventHub, String consumerGroup) {
        try (EventHubConsumerClient consumerClient = new EventHubClientBuilder()
            .connectionString(connectionString, eventHub)
            .consumerGroup(consumerGroup)
            .buildConsumerClient()) {
            return (int) consumerClient.getEventHubProperties().getPartitionIds().stream().count();
        }
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    // Use enum for log process status
    private enum ProcessStatus {
        PROCESSING, PROCESSED
    }

    // Use enum for log process stage
    private enum ProcessStage {
        UPDATE_CHECKPOINT, SEND_EVENT_BATCH
    }
}
