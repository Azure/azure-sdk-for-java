// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import reactor.core.publisher.Flux;

/**
 * Sample demonstrates how to send an iterable of events to specific event hub partition by defining partition id using
 * {@link SendOptions#setPartitionId(String)}.
 */
public class PublishIterableEvents {

    /**
     * Main method to invoke this demo about how to send an iterable of events with partition id configured.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program was interrupted before completion.
     */
    public static void main(String[] args) throws InterruptedException {
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        // Set some custom retry options other than the default set.
        AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setDelay(Duration.ofSeconds(30))
            .setMaxRetries(2)
            .setMode(AmqpRetryMode.EXPONENTIAL);

        // Instantiate a client that will be used to call the service.
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(connectionString)
            .retry(retryOptions)
            .buildAsyncProducerClient();

        // Create an iterable of events to send. Note that the events in iterable should
        // fit in a single batch. If the events exceed the size of the batch, then send operation will fail.
        final Iterable<EventData> events = Flux.range(0, 100).map(number -> {
            final String contents = "event-data-" + number;
            return new EventData(contents.getBytes(UTF_8));
        }).collectList().block();

        // To send our events, we need to know what partition to send it to. For the sake of this example, we take the
        // first partition id.
        CountDownLatch countDownLatch = new CountDownLatch(1);
        producer.getPartitionIds()
            .take(1) // take the first partition id
            .flatMap(partitionId -> producer.send(events, new SendOptions().setPartitionId(partitionId)))
            .subscribe(unused -> { },
                ex -> System.out.println("Failed to send events: " + ex.getMessage()),
                () -> {
                    countDownLatch.countDown();
                    System.out.println("Sending events completed successfully");
                });

        // Wait for async operation to complete or timeout after 10 seconds.
        try {
            countDownLatch.await(10, TimeUnit.SECONDS);
        } finally {
            producer.close();
        }
    }
}
