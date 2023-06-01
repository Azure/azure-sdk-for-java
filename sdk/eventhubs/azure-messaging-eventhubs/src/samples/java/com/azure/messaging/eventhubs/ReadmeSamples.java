// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    /**
     * Code sample for consuming events from a specific partition.
     */
    public void consumeEventsFromPartition() {
        // BEGIN: readme-sample-consumeEventsFromPartition
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                new DefaultAzureCredentialBuilder().build())
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();

        // Receive newly added events from partition with id "0". EventPosition specifies the position
        // within the Event Hub partition to begin consuming events.
        consumer.receiveFromPartition("0", EventPosition.latest()).subscribe(event -> {
            // Process each event as it arrives.
        });
        // add sleep or System.in.read() to receive events before exiting the process.
        // END: readme-sample-consumeEventsFromPartition
    }

    /**
     * Code sample for consuming events from synchronous client.
     */
    public void consumeEventsFromPartitionUsingSyncClient() {
        // BEGIN: readme-sample-consumeEventsFromPartitionUsingSyncClient
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        EventHubConsumerClient consumer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildConsumerClient();

        String partitionId = "<< EVENT HUB PARTITION ID >>";

        // Get the first 15 events in the stream, or as many events as can be received within 40 seconds.
        IterableStream<PartitionEvent> events = consumer.receiveFromPartition(partitionId, 15,
            EventPosition.earliest(), Duration.ofSeconds(40));
        for (PartitionEvent event : events) {
            System.out.println("Event: " + event.getData().getBodyAsString());
        }
        // END: readme-sample-consumeEventsFromPartitionUsingSyncClient
    }

    /**
     * Code sample for using Event Processor to consume events.
     * @throws InterruptedException if the thread is interrupted.
     */
    public void consumeEventsUsingEventProcessor() throws InterruptedException {
        // BEGIN: readme-sample-consumeEventsUsingEventProcessor
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup("<< CONSUMER GROUP NAME >>")
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .checkpointStore(new SampleCheckpointStore())
            .processEvent(eventContext -> {
                System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                    + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
            })
            .processError(errorContext -> {
                System.out
                    .println("Error occurred while processing events " + errorContext.getThrowable().getMessage());
            })
            .buildEventProcessorClient();

        // This will start the processor. It will start processing events from all partitions.
        eventProcessorClient.start();

        // (for demo purposes only - adding sleep to wait for receiving events)
        TimeUnit.SECONDS.sleep(2);

        // This will stop processing events.
        eventProcessorClient.stop();
        // END: readme-sample-consumeEventsUsingEventProcessor
    }
}

