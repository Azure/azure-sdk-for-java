// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to receive events from an Azure Event Hub instance.
 */
public class ConsumeEvents {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);
    private static final int NUMBER_OF_EVENTS = 10;

    /**
     * Main method to invoke this demo about how to receive events from an Azure Event Hub instance.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException The countdown latch was interrupted while waiting for this sample to
     *         complete.
     */
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_EVENTS);

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        // Instantiate a client that will be used to call the service.
        // Create a consumer.
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page. EventPosition.latest() tells the
        // service we only want events that are sent to the partition after we begin listening.
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString(connectionString)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();

        // To create a consumer, we need to know what partition to connect to. We take the first partition id.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = consumer.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

        // This shouldn't happen, but if we are unable to get the partitions within the timeout period.
        if (firstPartition == null) {
            firstPartition = "0";
        }

        // We start receiving any events that come from `firstPartition`, print out the contents, and decrement the
        // countDownLatch.
        Disposable subscription = consumer.receiveFromPartition(firstPartition, EventPosition.latest())
            .subscribe(partitionEvent -> {
                EventData event = partitionEvent.getData();
                String contents = new String(event.getBody(), UTF_8);
                System.out.println(String.format("[%s] Sequence Number: %s. Contents: %s", countDownLatch.getCount(),
                    event.getSequenceNumber(), contents));

                countDownLatch.countDown();
            });

        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildAsyncProducerClient();

        // Because the consumer is only listening to new events, we need to send some events to `firstPartition`.
        // We set the send options to send the events to `firstPartition`.
        SendOptions sendOptions = new SendOptions().setPartitionId(firstPartition);

        // We create 10 events to send to the service and block until the send has completed.
        Flux.range(0, NUMBER_OF_EVENTS).flatMap(number -> {
            String body = String.format("Hello world! Number: %s", number);
            return producer.send(new EventData(body.getBytes(UTF_8)), sendOptions);
        }).blockLast(OPERATION_TIMEOUT);

        try {
            // We wait for all the events to be received before continuing.
            boolean isSuccessful = countDownLatch.await(OPERATION_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            if (!isSuccessful) {
                System.err.printf("Did not complete successfully. There are: %s events left.%n",
                    countDownLatch.getCount());
            }
        } finally {
            // Dispose and close of all the resources we've created.
            subscription.dispose();
            producer.close();
            consumer.close();
        }
    }
}
