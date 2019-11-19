// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import reactor.core.Disposable;

import java.time.Duration;
import java.util.concurrent.Semaphore;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to receive events starting from the specific sequence number position in an Event Hub instance.
 */
public class ConsumeEventsFromKnownSequenceNumberPosition {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);
    private static long lastEnqueuedSequenceNumber = -1;
    private static String lastEnqueuedSequencePartitionId = null;

    /**
     * Main method to invoke this demo about how to receive event from a known sequence number position in an Azure Event Hub instance.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException The countdown latch was interrupted while waiting for this sample to
     *         complete.
     */
    public static void main(String[] args) throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString(connectionString)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .startingPosition(EventPosition.earliest());

        EventHubConsumerAsyncClient earliestConsumer = builder.buildAsyncConsumer();

        earliestConsumer.getPartitionIds().flatMap(partitionId -> earliestConsumer.getPartitionProperties(partitionId))
            .subscribe(
                properties -> {
                    if (!properties.isEmpty()) {
                        lastEnqueuedSequenceNumber = properties.getLastEnqueuedSequenceNumber();
                        lastEnqueuedSequencePartitionId = properties.getId();
                    }
                },
                error -> System.err.println("Error occurred while fetching partition properties: " + error.toString()),
                () -> {
                    // Releasing the semaphore now that we've finished querying for partition properties.
                    semaphore.release();
                });

        System.out.println("Waiting for partition properties to complete...");
        // Acquiring the semaphore so that this sample does not end before all the partition properties are fetched.
        semaphore.acquire();
        System.out.printf("Last enqueued sequence number: %s%n", lastEnqueuedSequenceNumber);

        // Make sure to have at least one non-empty event hub in order to continue the sample execution
        // if you don't have an non-empty event hub, try with another example 'SendEvent' in the same directory.
        if (lastEnqueuedSequenceNumber == -1 || lastEnqueuedSequencePartitionId == null) {
            System.err.println("All event hubs are empty");
            System.exit(0);
        }

        // Create a consumer.
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page. EventPosition.latest() tells the
        // service we only want events that are sent to the partition after we begin listening.
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString(connectionString)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .startingPosition(EventPosition.fromSequenceNumber(lastEnqueuedSequenceNumber, false))
            .buildAsyncConsumer();

        // We start receiving any events that come from `firstPartition`, print out the contents, and decrement the
        // countDownLatch.
        Disposable subscription = consumer.receive(lastEnqueuedSequencePartitionId).subscribe(partitionEvent -> {
            EventData event = partitionEvent.getData();
            String contents = UTF_8.decode(event.getBody()).toString();
            // ex. The last enqueued sequence number is 99. If isInclusive is true, the received event starting from the same
            // event with sequence number of '99'. Otherwise, the event with sequence number of '100' will be the first
            // event received.
            System.out.println(String.format("Receiving an event starting from the sequence number: %s. Contents: %s",
                event.getSequenceNumber(), contents));

            semaphore.release();
        });

        EventHubProducerAsyncClient producer = builder.buildAsyncProducer();

        // Because the consumer is only listening to new events, we need to send some events to that partition.
        // This sends the events to `lastEnqueuedSequencePartitionId`.
        SendOptions sendOptions = new SendOptions().setPartitionId(lastEnqueuedSequencePartitionId);

        producer.send(new EventData("Hello world!".getBytes(UTF_8)), sendOptions).block(OPERATION_TIMEOUT);
        // Acquiring the semaphore so that this sample does not end before all events are fetched.
        semaphore.acquire();

        // Dispose and close of all the resources we've created.
        subscription.dispose();
        producer.close();
        consumer.close();
        earliestConsumer.close();
    }
}
