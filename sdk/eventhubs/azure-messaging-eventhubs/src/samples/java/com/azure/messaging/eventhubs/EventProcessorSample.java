// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Sample code to demonstrate how a customer might use {@link EventProcessor}.
 */
public class EventProcessorSample {

    private static final String EH_CONNECTION_STRING = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

    /**
     * Main method to demonstrate starting and stopping a {@link EventProcessor}.
     *
     * @param args The input arguments to this executable.
     * @throws Exception If there are any errors while running the {@link EventProcessor}.
     */
    public static void main(String[] args) throws Exception {

        Logger logger = LoggerFactory.getLogger(EventProcessorSample.class);
        Function<PartitionEvent, Mono<Void>> processEvent = partitionEvent -> {
            logger.info(
                "Processing event: Event Hub name = {}; consumer group name = {}; partition id = {}; sequence number = {}",
                partitionEvent.getPartitionContext().getEventHubName(),
                partitionEvent.getPartitionContext().getConsumerGroup(),
                partitionEvent.getPartitionContext().getPartitionId(),
                partitionEvent.getEventData().getSequenceNumber());
            return partitionEvent.getPartitionContext().updateCheckpoint(partitionEvent.getEventData());
        };

        EventProcessorBuilder eventProcessorBuilder = new EventProcessorBuilder()
            .consumerGroup(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME)
            .connectionString(EH_CONNECTION_STRING)
            .processEvent(processEvent)
            .eventProcessorStore(new InMemoryEventProcessorStore());

        EventProcessor eventProcessor = eventProcessorBuilder.buildEventProcessor();
        System.out.println("Starting event processor");
        eventProcessor.start();
        eventProcessor.start(); // should be a no-op

        // Continue to perform other tasks while the processor is running in the background.
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));

        System.out.println("Stopping event processor");
        eventProcessor.stop();

        Thread.sleep(TimeUnit.SECONDS.toMillis(40));
        System.out.println("Starting a new instance of event processor");
        eventProcessor = eventProcessorBuilder.buildEventProcessor();
        eventProcessor.start();
        // Continue to perform other tasks while the processor is running in the background.
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
        System.out.println("Stopping event processor");
        eventProcessor.stop();
        System.out.println("Exiting process");
    }
}
