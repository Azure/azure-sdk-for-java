// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to receive events starting from the specific sequence number position in an Event Hub
 * instance. It also demonstrates how to publish events to a specific partition.
 */
public class ConsumeEventsFromKnownSequenceNumberPosition {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Main method to invoke this demo about how to receive event from a known sequence number position in an Azure
     * Event Hub instance.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        final AtomicBoolean isRunning = new AtomicBoolean(true);

        // The credential used is DefaultAzureCredential because it combines commonly used credentials
        // in deployment and development and chooses the credential to used based on its running environment.
        // More information can be found at: https://learn.microsoft.com/java/api/overview/azure/identity-readme
        final TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Create a builder that both consumer and producer will be instantiated from.
        //
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                tokenCredential);

        // The consumer group is required for consuming events.
        //
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page.
        final EventHubConsumerAsyncClient consumer = builder
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();

        // Find the first non-empty partition we can start consuming from.
        // Block on it because we don't know what partition to start reading from, yet.
        final PartitionProperties nonEmptyPartition = consumer.getPartitionIds()
            .flatMap(partitionId -> consumer.getPartitionProperties(partitionId))
            .filter(properties -> !properties.isEmpty())
            .blockFirst(OPERATION_TIMEOUT);

        // Make sure to have at least one non-empty event hub in order to continue the sample execution
        // if you don't have an non-empty event hub, try with another example 'SendEvent' in the same directory.
        if (nonEmptyPartition == null) {
            System.err.println("All event hub partitions are empty");
            System.exit(0);
        }

        // ex. The last enqueued sequence number is 99. If isInclusive is true, the received event starting from
        // the same event with sequence number of '99'. Otherwise, the event with sequence number of '100' will
        //  be the first event received.
        final EventPosition position = EventPosition.fromSequenceNumber(
            nonEmptyPartition.getLastEnqueuedSequenceNumber(), true);

        // We start receiving any events that come from that non-empty partition, print out the contents.
        // We keep receiving events while `takeWhile` resolves to true, that is, the program is still running.
        consumer.receiveFromPartition(nonEmptyPartition.getId(), position)
            .takeWhile(ignored -> isRunning.get())
            .subscribe(partitionEvent -> {
                EventData event = partitionEvent.getData();
                String contents = new String(event.getBody(), UTF_8);

                System.out.println(String.format("Event sequence number number: %s. Contents: %s%n",
                    event.getSequenceNumber(), contents));
            });

        // Create a producer.
        final EventHubProducerClient producer = builder.buildProducerClient();

        // Because the consumer is only listening to new events after the last enqueued event was received, we need to
        // send some events to that partition.
        final SendOptions sendOptions = new SendOptions().setPartitionId(nonEmptyPartition.getId());
        producer.send(Collections.singletonList(new EventData("Hello world!".getBytes(UTF_8))), sendOptions);

        // Set isRunning to false so we stop taking events.
        isRunning.set(false);

        // Dispose and close of all the resources we've created.
        producer.close();
        consumer.close();
    }
}
