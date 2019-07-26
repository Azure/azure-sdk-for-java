// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to receive events from an Azure Event Hub instance.
 */
public class ConsumeEvent {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);
    private static final int NUMBER_OF_EVENTS = 10;

    /**
     * Main method to invoke this demo about how to receive events from an Azure Event Hub instance.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException The countdown latch was interrupted while waiting for this sample to
     *         complete.
     * @throws IOException If we were unable to dispose of the {@link EventHubAsyncClient}, {@link EventHubConsumer},
     *         or the {@link EventHubProducer}
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_EVENTS);

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";

        // Instantiate a client that will be used to call the service.
        EventHubAsyncClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        // To create a consumer, we need to know what partition to connect to. We take the first partition id.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = client.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

        // Create a consumer.
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page. EventPosition.latest() tells the
        // service we only want events that are sent to the partition after we begin listening.
        EventHubConsumer consumer = client.createConsumer(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME,
            firstPartition, EventPosition.latest());

        // We start receiving any events that come from `firstPartition`, print out the contents, and decrement the
        // countDownLatch.
        Disposable subscription = consumer.receive().subscribe(event -> {
            String contents = UTF_8.decode(event.body()).toString();
            System.out.println(String.format("[%s] Sequence Number: %s. Contents: %s", countDownLatch.getCount(),
                event.sequenceNumber(), contents));

            countDownLatch.countDown();
        });

        // Because the consumer is only listening to new events, we need to send some events to `firstPartition`.
        // This creates a producer that only sends events to `firstPartition`.
        EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(firstPartition);
        EventHubProducer producer = client.createProducer(producerOptions);

        // We create 10 events to send to the service and block until the send has completed.
        Flux.range(0, NUMBER_OF_EVENTS).flatMap(number -> {
            String body = String.format("Hello world! Number: %s", number);
            return producer.send(new EventData(body.getBytes(UTF_8)));
        }).blockLast(OPERATION_TIMEOUT);

        try {
            // We wait for all the events to be received before continuing.
            boolean isSuccessful = countDownLatch.await(OPERATION_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            if (!isSuccessful) {
                System.err.printf("Did not complete successfully. There are: %s events left.", countDownLatch.getCount());
            }
        } finally {
            // Dispose and close of all the resources we've created.
            subscription.dispose();
            producer.close();
            consumer.close();
            client.close();
        }
    }
}
