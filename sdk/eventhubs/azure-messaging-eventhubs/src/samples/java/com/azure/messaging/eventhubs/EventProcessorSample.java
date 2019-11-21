// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.PartitionEvent;
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
        Consumer<PartitionEvent> processEvent = partitionEvent -> {
            logger.info(
                "Processing event: Event Hub name = {}; consumer group name = {}; partition id = {}; sequence number = {}",
                partitionEvent.getPartitionContext().getEventHubName(),
                partitionEvent.getPartitionContext().getConsumerGroup(),
                partitionEvent.getPartitionContext().getPartitionId(),
                partitionEvent.getData().getSequenceNumber());
            partitionEvent.getPartitionContext().updateCheckpoint(partitionEvent.getData()).subscribe();
        };

        EventProcessorClientBuilder eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .connectionString(EH_CONNECTION_STRING)
            .processEvent(processEvent)
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
