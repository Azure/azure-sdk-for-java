// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
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
        // The required parameters are startingPosition, consumerGroup, and a way to authenticate with Event Hubs
        // using credentials.
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("fake-string")
            .startingPosition(EventPosition.latest())
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumer();
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.instantiation
    }
    /**
     * Receives event data
     */
    public void receive() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("fake-string")
            .startingPosition(EventPosition.latest())
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumer();

        // Obtain partitionId from EventHubConsumerAsyncClient.getPartitionIds()
        String partitionId = "0";

        // Keep a reference to `subscription`. When the program is finished receiving events, call
        // subscription.dispose(). This will stop fetching events from the Event Hub.
        Disposable subscription = consumer.receive(partitionId).subscribe(event -> {
            // process event
        }, error -> System.err.print(error.toString()));
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string

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
            .startingPosition(EventPosition.latest())
            .buildAsyncConsumer();

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-basesubscriber
        consumer.receive(partitionId).subscribe(new BaseSubscriber<PartitionEvent>() {
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
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-basesubscriber
    }

    /**
     * Receives from all partitions.
     */
    public void receiveAll() {
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("fake-string")
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .startingPosition(EventPosition.latest())
            .buildAsyncConsumer();

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive
        // Receives events from all partitions as they come in.
        consumer.receive().subscribe(partitionEvent -> {
            PartitionContext context = partitionEvent.getPartitionContext();
            EventData event = partitionEvent.getEventData();
            System.out.printf("Event %s is from partition %s%n.", event.getSequenceNumber(), context.getPartitionId());
        });
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive
    }

    /**
     * Receives from all partitions.
     */
    public void receiveLastEnqueuedInformation() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#lastenqueuedeventproperties
        EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setTrackLastEnqueuedEventProperties(true);
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("fake-string")
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .startingPosition(EventPosition.latest())
            .consumerOptions(options)
            .buildAsyncConsumer();

        // Receives events from all partitions as they come in.
        consumer.receive().subscribe(partitionEvent -> {
            PartitionContext context = partitionEvent.getPartitionContext();
            LastEnqueuedEventProperties properties = context.getLastEnqueuedEventProperties();
            System.out.printf("Information received at %s. Sequence Id: %s", properties.getRetrievalTime(),
                properties.getSequenceNumber());
        });
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#lastenqueuedeventproperties
    }
}
