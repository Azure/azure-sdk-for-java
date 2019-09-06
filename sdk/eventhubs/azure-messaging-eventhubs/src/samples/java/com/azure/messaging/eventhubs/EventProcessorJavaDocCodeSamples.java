// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.EventProcessorBuilderJavaDocCodeSamples.PartitionProcessorImpl;

/**
 * Code snippets for {@link EventProcessor}.
 */
public final class EventProcessorJavaDocCodeSamples {

    /**
     * Code snippet for showing how to start and stop an {@link EventProcessor}.
     */
    public void startStopSample() {
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";
        EventHubAsyncClient eventHubAsyncClient = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        EventProcessor eventProcessor = new EventProcessorBuilder()
            .eventHubClient(eventHubAsyncClient)
            .partitionProcessorFactory((PartitionProcessorImpl::new))
            .consumerGroup("consumer-group")
            .buildEventProcessor();

        // BEGIN: com.azure.messaging.eventhubs.eventprocessor.startstop
        eventProcessor.start();
        // do other stuff while the event processor is running
        eventProcessor.stop();
        // END: com.azure.messaging.eventhubs.eventprocessor.startstop
    }
}
