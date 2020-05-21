// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample code to demonstrate how to process batch of events using {@link EventProcessorClient}.
 */
public class EventProcessorClientWithBatchReceiveSample {

    private static final String EH_CONNECTION_STRING = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

    /**
     * Main method to demonstrate starting and stopping a {@link EventProcessorClient}.
     *
     * @param args The input arguments to this executable.
     * @throws Exception If there are any errors while running the {@link EventProcessorClient}.
     */
    public static void main(String[] args) throws Exception {

        Logger logger = LoggerFactory.getLogger(EventProcessorClientSample.class);
        Consumer<EventBatchContext> processEventBatch = eventBatchContext -> {
            eventBatchContext.getEvents().forEach(event -> {
                logger.info(
                    "Processing event: Event Hub name = {}; consumer group name = {}; partition id = {}; sequence number = {}",
                    eventBatchContext.getPartitionContext().getEventHubName(),
                    eventBatchContext.getPartitionContext().getConsumerGroup(),
                    eventBatchContext.getPartitionContext().getPartitionId(),
                    event.getSequenceNumber());
            });
            eventBatchContext.updateCheckpoint();
        };

        // This error handler logs the error that occurred and keeps the processor running. If the error occurred in
        // a specific partition and had to be closed, the ownership of the partition will be given up and will allow
        // other processors to claim ownership of the partition.
        Consumer<ErrorContext> processError = errorContext -> {
            logger.error("Error while processing {}, {}, {}, {}", errorContext.getPartitionContext().getEventHubName(),
                errorContext.getPartitionContext().getConsumerGroup(),
                errorContext.getPartitionContext().getPartitionId(),
                errorContext.getThrowable().getMessage());
        };

        // Setup the builder to create an Event Processor that processs 50 events in a batch or waits upto a max
        // of 30 seconds before processing any available events upto that point. The batch could be empty if
        // no events are received within that 30-second window.
        EventProcessorClientBuilder eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .connectionString(EH_CONNECTION_STRING)
            .processEventBatch(processEventBatch, 50, Duration.ofSeconds(30))
            .processError(processError)
            .checkpointStore(new SampleCheckpointStore());

        EventProcessorClient eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();
        System.out.println("Starting event processor");
        eventProcessorClient.start();

        // Continue to perform other tasks while the processor is running in the background.
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));

        System.out.println("Stopping event processor");
        eventProcessorClient.stop();
        System.out.println("Exiting process");
    }

}
