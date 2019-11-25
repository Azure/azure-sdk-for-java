// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Code snippets demonstrating various {@link EventHubConsumerAsyncClient} scenarios.
 */
public class EventHubConsumerAsyncClientJavaDocCodeSamples {

    public void initialization() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.instantiation
        // The required parameters are `consumerGroup` and a way to authenticate with Event Hubs using credentials.
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("event-hub-instance-connection-string")
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.instantiation
    }

    /**
     * Receives event data from a single partition.
     */
    public void receive() {
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("fake-string")
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition
        // Obtain partitionId from EventHubConsumerAsyncClient.getPartitionIds()
        String partitionId = "0";
        EventPosition startingPosition = EventPosition.latest();

        // Keep a reference to `subscription`. When the program is finished receiving events, call
        // subscription.dispose(). This will stop fetching events from the Event Hub.
        Disposable subscription = consumer.receiveFromPartition(partitionId, startingPosition)
            .subscribe(partitionEvent -> {
                PartitionContext partitionContext = partitionEvent.getPartitionContext();
                EventData event = partitionEvent.getData();

                System.out.printf("Received event from partition '%s'%n", partitionContext.getPartitionId());
                System.out.printf("Contents of event as string: '%s'%n", event.getBodyAsString());
            }, error -> System.err.print(error.toString()));
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition

        // When program ends, or you're done receiving all events.
        subscription.dispose();
    }

    /**
     * Receives event data with back pressure.
     */
    public void receiveBackpressure() {
        // Obtain partitionId from EventHubAsyncClient.getPartitionIds()
        String partitionId = "0";

        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("fake-string")
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition-basesubscriber
        consumer.receiveFromPartition(partitionId, EventPosition.latest()).subscribe(new BaseSubscriber<PartitionEvent>() {
            private static final int NUMBER_OF_EVENTS = 5;
            private final AtomicInteger currentNumberOfEvents = new AtomicInteger();

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                // Tell the Publisher we only want 5 events at a time.
                request(NUMBER_OF_EVENTS);
            }

            @Override
            protected void hookOnNext(PartitionEvent value) {
                // Process the EventData

                // If the number of events we have currently received is a multiple of 5, that means we have reached the
                // last event the Publisher will provide to us. Invoking request(long) here, tells the Publisher that
                // the subscriber is ready to get more events from upstream.
                if (currentNumberOfEvents.incrementAndGet() % 5 == 0) {
                    request(NUMBER_OF_EVENTS);
                }
            }
        });
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition-basesubscriber
    }

    /**
     * Receives from all partitions.
     */
    public void receiveAll() {
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("fake-string")
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#boolean
        // Receives events from all partitions from the beginning of each partition.
        consumer.receive(true).subscribe(partitionEvent -> {
            PartitionContext context = partitionEvent.getPartitionContext();
            EventData event = partitionEvent.getData();
            System.out.printf("Event %s is from partition %s%n.", event.getSequenceNumber(), context.getPartitionId());
        });
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#boolean
    }

    /**
     * Receives from all partitions with last enqueued information.
     */
    public void receiveLastEnqueuedInformation() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#boolean-receiveoptions
        ReceiveOptions receiveOptions = new ReceiveOptions()
            .setTrackLastEnqueuedEventProperties(true);
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("event-hub-instance-connection-string")
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();

        // Receives events from all partitions as they come in.
        consumer.receive(false, receiveOptions).subscribe(partitionEvent -> {
            LastEnqueuedEventProperties properties = partitionEvent.getLastEnqueuedEventProperties();
            System.out.printf("Information received at %s. Sequence Id: %s%n", properties.getRetrievalTime(),
                properties.getSequenceNumber());
        });
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#boolean-receiveoptions
    }
}
