// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import java.util.concurrent.TimeUnit;

/**
 * Sample code to demonstrate how a customer might use {@link EventProcessor}.
 */
public class EventProcessorSample {

    private static final String EH_CONNECTION_STRING = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

    /**
     * Main method to demonstrate starting and stopping a {@link EventProcessor}.
     *
     * @param args The input arguments to this executable.
     * @throws Exception If there are any errors while running the {@link EventProcessor}.
     */
    public static void main(String[] args) throws Exception {
        EventHubClientBuilder eventHubClientBuilder = new EventHubClientBuilder()
            .connectionString(EH_CONNECTION_STRING)
            .consumerGroupName(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME)
            .partitionProcessorFactory(LogPartitionProcessor::new)
            .partitionManager(new InMemoryPartitionManager());

        EventProcessor eventProcessor = eventHubClientBuilder.buildEventProcessor();
        System.out.println("Starting event processor");
        eventProcessor.start();
        eventProcessor.start(); // should be a no-op

        // do other stuff
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));

        System.out.println("Stopping event processor");
        eventProcessor.stop();

        Thread.sleep(TimeUnit.SECONDS.toMillis(40));
        System.out.println("Starting a new instance of event processor");
        eventProcessor = eventHubClientBuilder.buildEventProcessor();
        eventProcessor.start();
        // do other stuff
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
        System.out.println("Stopping event processor");
        eventProcessor.stop();
        System.out.println("Exiting process");
    }
}
