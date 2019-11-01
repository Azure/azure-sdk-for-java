// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Code snippets demonstrating various {@link EventHubConsumerAsyncClient} scenarios.
 */
public class EventHubConsumerAsyncClientJavaDocCodeSamples {
    private final EventHubAsyncClient client = new EventHubClientBuilder().connectionString("fake-string").buildAsyncClient();

    /**
     * Receives event data
     */
    public void receive() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive
        // Obtain partitionId from EventHubAsyncClient.getPartitionIds()
        String partitionId = "0";

        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder().connectionString("fake-string")
            .startingPosition(EventPosition.latest())
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumer();

        // Keep a reference to `subscription`. When the program is finished receiving events, call
        // subscription.dispose(). This will stop fetching events from the Event Hub.
        Disposable subscription = consumer.receive(partitionId).subscribe(event -> {
            // process event
        }, error -> System.err.print(error.toString()));
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive

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

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#basesubscriber
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
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#basesubscriber
    }
}
