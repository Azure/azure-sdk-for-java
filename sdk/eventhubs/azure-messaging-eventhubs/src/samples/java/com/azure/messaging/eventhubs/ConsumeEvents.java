// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.SendOptions;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
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
     *
     * @throws InterruptedException The countdown latch was interrupted while waiting for this sample to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_EVENTS);

        // The credential used is DefaultAzureCredential because it combines commonly used credentials
        // in deployment and development and chooses the credential to used based on its running environment.
        // More information can be found at: https://learn.microsoft.com/java/api/overview/azure/identity-readme
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Create a consumer.
        //
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page.
        //
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                tokenCredential)
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
        // countDownLatch.  EventPosition.latest() tells the service we only want events that are sent to the partition
        // AFTER we begin listening.
        Disposable subscription = consumer.receiveFromPartition(firstPartition, EventPosition.latest())
            .subscribe(partitionEvent -> {
                EventData event = partitionEvent.getData();
                PartitionContext partitionContext = partitionEvent.getPartitionContext();

                String contents = new String(event.getBody(), UTF_8);
                System.out.printf("[#%s] Partition id: %s. Sequence Number: %s. Contents: '%s'%n",
                    countDownLatch.getCount(), partitionContext.getPartitionId(), event.getSequenceNumber(),
                    contents);

                countDownLatch.countDown();
            },
                error -> {
                    System.err.println("Error occurred while consuming events: " + error);

                    // Count down until 0, so the main thread does not keep waiting for events.
                    while (countDownLatch.getCount() > 0) {
                        countDownLatch.countDown();
                    }
                }, () -> {
                    System.out.println("Finished reading events.");
                });

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                tokenCredential)
            .buildAsyncProducerClient();

        // Because the consumer is only listening to new events, we need to send some events to `firstPartition`.
        // We set the send options to send the events to `firstPartition`.
        SendOptions sendOptions = new SendOptions().setPartitionId(firstPartition);

        // We create 10 events to send to the service and block until the send has completed.
        Flux.range(0, NUMBER_OF_EVENTS).flatMap(number -> {
            String body = String.format("Hello world! Number: %s", number);
            return producer.send(Collections.singletonList(new EventData(body.getBytes(UTF_8))), sendOptions);
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
