// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.CloseReason;
import com.azure.messaging.eventhubs.models.PartitionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrating how to maintain processing state. Counts the number of events that were processed for each
 * partition.
 *
 * @see EventProcessorClientAggregateEventsSample Another sample demonstrating state management.
 */
public class EventProcessorClientStateManagement {
    private static final String EH_CONNECTION_STRING = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
        + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

    private final Logger logger = LoggerFactory.getLogger("Processor");
    /**
     * Keeps track of the number of events processed from each partition.
     * Key: Partition id
     * Value: Number of events processed for each partition.
     */
    private final ConcurrentHashMap<String, Integer> eventsProcessed = new ConcurrentHashMap<>();

    /**
     * Main method to demonstrate starting and stopping a {@link EventProcessorClient}.
     *
     * @param args The input arguments to this executable.
     * @throws Exception If there are any errors while running the {@link EventProcessorClient}.
     */
    public static void main(String[] args) throws Exception {
        final EventProcessorClientStateManagement program = new EventProcessorClientStateManagement();
        final EventProcessorClient client = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .connectionString(EH_CONNECTION_STRING)
            .processPartitionInitialization(context -> program.onInitialize(context.getPartitionContext()))
            .processPartitionClose(context -> program.onClose(context.getPartitionContext(), context.getCloseReason()))
            .processEvent(event -> program.onEvent(event.getPartitionContext(), event.getEventData()))
            .processError(error -> program.onError(error.getPartitionContext(), error.getThrowable()))
            .checkpointStore(new SampleCheckpointStore())
            .buildEventProcessorClient();

        System.out.println("Starting event processor");
        client.start();

        // Continue to perform other tasks while the processor is running in the background.
        Thread.sleep(TimeUnit.SECONDS.toMillis(30));

        System.out.println("Stopping event processor");
        client.stop();
        System.out.println("Exiting process");
    }

    /**
     * When an occurs, reports that error to a log.
     *
     * @param partitionContext Context information for the partition in which this error occurred.
     * @param error Error that occurred.
     */
    void onError(PartitionContext partitionContext, Throwable error) {
        logger.error("Error occurred processing partition '{}'. Exception: {}", partitionContext.getPartitionId(),
            error);
    }

    /**
     * On initialisation, keeps track of which partitions it is processing.
     *
     * @param partitionContext Information about partition it is processing.
     */
    void onInitialize(PartitionContext partitionContext) {
        logger.info("Starting to process partition {}", partitionContext.getPartitionId());
        eventsProcessed.computeIfAbsent(partitionContext.getPartitionId(), unused -> 0);
    }

    /**
     * Invoked when a partition is no longer being processed.
     *
     * @param partitionContext Context information for the partition that is no longer being processed.
     * @param reason Reason for no longer processing partition.
     */
    void onClose(PartitionContext partitionContext, CloseReason reason) {
        logger.info("Stopping processing of partition {}. Reason: {}", partitionContext.getPartitionId(), reason);
        eventsProcessed.remove(partitionContext.getPartitionId());
    }

    /**
     * Processes an event from the partition. Aggregates the number of events that were processed in this partition.
     *
     * @param partitionContext Information about which partition this event was in.
     * @param eventData Event from the partition.
     */
    void onEvent(PartitionContext partitionContext, EventData eventData) {
        final Integer count = eventsProcessed.compute(partitionContext.getPartitionId(),
            (key, value) -> value == null ? 1 : value + 1);

        logger.info("Event {} received for partition: {}. # of events processed: {}",
            eventData.getSequenceNumber(), partitionContext.getPartitionId(), count);
    }
}
