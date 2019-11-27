// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Sample code to demonstrate how a customer might use {@link EventProcessorClient}.
 */
public class EventProcessorSample {

    private static final String EH_CONNECTION_STRING = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

    /**
     * Main method to demonstrate starting and stopping a {@link EventProcessorClient}.
     *
     * @param args The input arguments to this executable.
     * @throws Exception If there are any errors while running the {@link EventProcessorClient}.
     */
    public static void main(String[] args) throws Exception {

        Logger logger = LoggerFactory.getLogger(EventProcessorSample.class);
        Consumer<EventContext> processEvent = eventContext -> {
            logger.info(
                "Processing event: Event Hub name = {}; consumer group name = {}; partition id = {}; sequence number = {}",
                eventContext.getPartitionContext().getEventHubName(),
                eventContext.getPartitionContext().getConsumerGroup(),
                eventContext.getPartitionContext().getPartitionId(),
                eventContext.getEventData().getSequenceNumber());
            eventContext.updateCheckpoint();
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

        EventProcessorClientBuilder eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .connectionString(EH_CONNECTION_STRING)
            .processEvent(processEvent)
            .processError(processError)
            .checkpointStore(new InMemoryCheckpointStore());

        EventProcessorClient eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();
        System.out.println("Starting event processor");
        eventProcessorClient.start();
        eventProcessorClient.start(); // should be a no-op

        // Continue to perform other tasks while the processor is running in the background.
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));

        System.out.println("Stopping event processor");
        eventProcessorClient.stop();

        Thread.sleep(TimeUnit.SECONDS.toMillis(40));
        System.out.println("Starting a new instance of event processor");
        eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();
        eventProcessorClient.start();
        // Continue to perform other tasks while the processor is running in the background.
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
        System.out.println("Stopping event processor");
        eventProcessorClient.stop();
        System.out.println("Exiting process");
    }
}
