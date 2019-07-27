// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.eventprocessor;

import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorAsyncClient;

/**
 * Sample code to demonstrate how a customer might use {@link EventProcessorAsyncClient}.
 */
public class EventProcessorSample {

    public static void main(String[] args) throws Exception {
        EventProcessorAsyncClient eventProcessorAsyncClient = new EventHubClientBuilder()
            .connectionString("")
            .consumerGroupName(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME)
            .partitionProcessorFactory(ConsolePartitionProcessor::new)
            .partitionManager(new InMemoryPartitionManager())
            .buildEventProcessorAsyncClient();

        System.out.println("Starting event processor");
        eventProcessorAsyncClient.start();
        System.out.println("Event processor started");

        eventProcessorAsyncClient.start();

        // do other stuff
        Thread.sleep(30000);

        System.out.println("Stopping event processor");
        eventProcessorAsyncClient.stop();
        System.out.println("Stopped event processor");
        System.out.println("Exiting process");
    }
}
