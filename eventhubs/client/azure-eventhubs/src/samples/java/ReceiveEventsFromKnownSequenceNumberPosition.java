// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClient;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumer;
import com.azure.messaging.eventhubs.EventHubProducer;
import com.azure.messaging.eventhubs.EventHubProducerOptions;
import com.azure.messaging.eventhubs.EventPosition;
import reactor.core.Disposable;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to receive events starting from the specific sequence number position in an Event Hub instance.
 */
public class ReceiveEventsFromKnownSequenceNumberPosition {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);
    private static final int EVENT_BATCH_SIZE = 10;
    private static EventData thirdEvent;

    /**
     * Main method to invoke this demo about how to receive event from a known sequence number position in an Azure Event Hub instance.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException The countdown latch was interrupted while waiting for this sample to
     *         complete.
     * @throws IOException If we were unable to dispose of the {@link EventHubClient}, {@link EventHubConsumer},
     *         or the {@link EventHubProducer}
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        CountDownLatch countDownLatch = new CountDownLatch(EVENT_BATCH_SIZE);

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";

        // Instantiate a client that will be used to call the service.
        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .build();

        // To create a consumer, we need to know what partition to connect to. We take the first partition id.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = client.getPartitionIds().blockFirst(OPERATION_TIMEOUT);


        // Create a consumer.
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page. EventPosition.latest() tells the
        // service we only want events that are sent to the partition after we begin listening.
        EventHubConsumer consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
            firstPartition, EventPosition.latest());

        // We start receiving any events that come from `firstPartition`, print out the contents, and decrement the
        // countDownLatch.
        Disposable subscription = consumer.receive().subscribe(event -> {
            String contents = UTF_8.decode(event.body()).toString();
            if (countDownLatch.getCount() == 3) {
                thirdEvent = event;
            }
            System.out.println(String.format("[%s] Sequence Number: %s. Contents: %s", countDownLatch.getCount(),
                event.sequenceNumber(), contents));

            countDownLatch.countDown();
        });

        // Because the consumer is only listening to new events, we need to send some events to `firstPartition`.
        // This creates a producer that only sends events to `firstPartition`.
        EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(firstPartition);
        EventHubProducer producer = client.createProducer(producerOptions);

        // Create an event data list
        ArrayList<EventData> events = new ArrayList<>(EVENT_BATCH_SIZE);
        for (int i = 0; i < EVENT_BATCH_SIZE; i++) {
            events.add(new EventData(UTF_8.encode("I am Event " + i)));
        }

        // We create EVENT_BATCH_SIZE events to send to the service and block until the send has completed.
        producer.send(events).block(OPERATION_TIMEOUT);

        // We wait for all the events to be received before continuing.
        countDownLatch.await(OPERATION_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

        System.out.println("Third Event's sequence number is " + thirdEvent.sequenceNumber());

        Semaphore semaphore = new Semaphore(1);
        // Acquiring the semaphore so that this sample does not end before the receiver receives the events after the sequence event
        semaphore.acquire();

        // Create an event position from a specific sequence number of event.
        // If Inclusive is true, the event with the sequenceNumber is included; otherwise, the next event will be received.
        EventPosition exclusiveSequenceNumberPosition = EventPosition.fromSequenceNumber(thirdEvent.sequenceNumber(), false);

        // Create a consumer.
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page. EventPosition.fromSequenceNumber() tells the
        // service we only want events that are after the sequence number to the partition after we begin listening.
        EventHubConsumer newConsumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, firstPartition,
            exclusiveSequenceNumberPosition);

        Disposable newSubscription = newConsumer.receive().subscribe(event -> {
            String contents = UTF_8.decode(event.body()).toString();
            System.out.println(String.format("Sequence Number: %s. Contents: %s", event.sequenceNumber(), contents));
            // Releasing the semaphore now that we've finished querying for the event
            semaphore.release();
        });

        semaphore.acquire();

        // Dispose and close of all the resources we've created.
        subscription.dispose();
        newSubscription.dispose();
        consumer.close();
        newConsumer.close();
        producer.close();
        client.close();
    }
}
