// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.eventprocessor;

import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample code to demonstrate how a customer might use {@link EventProcessor}
 */
public class EventProcessorSample {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessorSample.class);

    public static void main(String[] args) throws Exception {
        EventProcessor eventProcessor = new EventHubClientBuilder()
            .connectionString(
                "Endpoint=sb://eventhubs-ns-playground-standard.servicebus.windows.net/;SharedAccessKeyName=srnagarcspolicy;SharedAccessKey=tm73rARY77e1FakWDVxsm13yfn5A4ypmtDuOXNQ4RvM=;EntityPath=srnagar-hub")
            .consumerGroupName(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME)
            .partitionProcessorFactory(ConsolePartitionProcessor::new)
            .partitionManager(new InMemoryPartitionManager())
            .buildEventProcessorAsyncClient();

        LOGGER.info("Starting event processor");
        eventProcessor.start();
        LOGGER.info("Event processor started");

        // do other stuff
        Thread.sleep(30000);

        LOGGER.info("Stopping event processor");
        eventProcessor.stop();
        LOGGER.info("Stopped event processor");
        LOGGER.info("Exiting process");
    }
}
