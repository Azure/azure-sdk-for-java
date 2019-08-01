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

    //    private static final String EH_CONNECTION_STRING = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";
    private static final String EH_CONNECTION_STRING = "Endpoint=sb://eventhubs-ns-playground-standard.servicebus.windows.net/;SharedAccessKeyName=srnagarcspolicy;SharedAccessKey=tm73rARY77e1FakWDVxsm13yfn5A4ypmtDuOXNQ4RvM=;EntityPath=srnagar-hub";

    public static void main(String[] args) throws Exception {
        EventProcessorAsyncClient eventProcessorAsyncClient = new EventHubClientBuilder()
            .connectionString(EH_CONNECTION_STRING)
            .consumerGroupName(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME)
            .partitionProcessorFactory(LogPartitionProcessor::new)
            .partitionManager(new InMemoryPartitionManager())
            .buildEventProcessorAsyncClient();

        System.out.println("Starting event processor");
        eventProcessorAsyncClient.start();
        System.out.println("Event processor started");

        eventProcessorAsyncClient.start();

        // do other stuff
        Thread.sleep(70000);

        System.out.println("Stopping event processor");
        eventProcessorAsyncClient.stop().subscribe();
        System.out.println("Stopped event processor");
        System.out.println("Exiting process");
    }
}
