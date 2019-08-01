// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import java.util.concurrent.TimeUnit;

/**
 * Sample code to demonstrate how a customer might use {@link EventProcessorAsyncClient}.
 */
public class EventProcessorSample {
//    private static final String EH_CONNECTION_STRING = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";
    private static final String EH_CONNECTION_STRING = "Endpoint=sb://js-event-processor-hubs.servicebus.windows.net/;SharedAccessKeyName=java-eph;SharedAccessKey=OIJpa+g4jJSm8WoSuk5y+b1XTRsxHqNhygBCGc4yTw8=;EntityPath=mega-event-hub";

    public static void main(String[] args) throws Exception {
        EventHubClientBuilder eventHubClientBuilder = new EventHubClientBuilder()
            .connectionString(EH_CONNECTION_STRING)
            .consumerGroupName(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME)
            .partitionProcessorFactory(LogPartitionProcessor::new)
            .partitionManager(new InMemoryPartitionManager());

        EventProcessorAsyncClient eventProcessorAsyncClient = eventHubClientBuilder.buildEventProcessorAsyncClient();
        System.out.println("Starting event processor");
        eventProcessorAsyncClient.start();
        eventProcessorAsyncClient.start(); // should be a no-op

        // do other stuff
        Thread.sleep(TimeUnit.MINUTES.toMillis(5));

        System.out.println("Stopping event processor");
        eventProcessorAsyncClient.stop().subscribe();

//        Thread.sleep(TimeUnit.SECONDS.toMillis(40));
//        System.out.println("Starting a new instance of event processor");
//        eventProcessorAsyncClient = eventHubClientBuilder.buildEventProcessorAsyncClient();
//        eventProcessorAsyncClient.start();
//        // do other stuff
//        Thread.sleep(70000);
//        System.out.println("Stopping event processor");
//        eventProcessorAsyncClient.stop().subscribe();
        System.out.println("Exiting process");
    }
}
