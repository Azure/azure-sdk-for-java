// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Code snippets demonstrating various {@link EventHubConsumer} scenarios.
 */
public class EventHubConsumerJavaDocCodeSnippets {
    private final EventHubClient client = new EventHubClientBuilder().connectionString("fake-string").build();

    /**
     * Receives event data
     */
    public void receive() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumer.receive
        // Obtain partitionId from EventHubClient.getPartitionIds()
        String partitionId = "0";

        EventHubConsumer consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionId,
            EventPosition.latest());

        // Keep a reference to `subscription`. When the program is finished receiving events, call
        // subscription.dispose(). This will stop fetching events from the Event Hub.
        Disposable subscription = consumer.receive().subscribe(event -> {
            // process event
        }, error -> {
            System.err.print(error.toString());
        });
        // END: com.azure.messaging.eventhubs.eventhubconsumer.receive
    }

    /**
     * Receives event data with back pressure.
     */
    public void receiveBackpressure() {
        // Obtain partitionId from EventHubClient.getPartitionIds()
        String partitionId = "0";

        EventHubConsumer consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionId,
            EventPosition.latest());

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumer.receiveBackpressure
        consumer.receive().subscribe(new BaseSubscriber<EventData>() {
            private final AtomicInteger currentNumberOfEvents = new AtomicInteger();
            private final int numberOfEvents = 5;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                // Tell the Publisher we only want 5 events at a time.
                request(numberOfEvents);
            }

            @Override
            protected void hookOnNext(EventData value) {
                // Process the EventData

                // If the number of events we have currently received is a multiple of 5, that means we have reached the
                // last event the Publisher will provide to us. Invoking request(long) here, tells the Publisher that
                // the subscriber is ready to get more events from upstream.
                if (currentNumberOfEvents.incrementAndGet() % 5 == 0) {
                    request(numberOfEvents);
                }
            }
        });
        // END: com.azure.messaging.eventhubs.eventhubconsumer.receiveBackpressure
    }
}
