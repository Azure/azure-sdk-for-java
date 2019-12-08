// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

/**
 * Code snippets for {@link EventProcessorClient}.
 */
public final class EventProcessorJavaDocCodeSamples {

    /**
     * Code snippet for showing how to start and stop an {@link EventProcessorClient}.
     */
    public void startStopSample() {
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .connectionString(connectionString)
            .processEvent(eventContext -> {
                System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                    + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
            })
            .processError(errorContext -> {
                System.out.printf("Error occurred in partition processor for partition {}, {}",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
            })
            .consumerGroup("consumer-group")
            .buildEventProcessorClient();

        // BEGIN: com.azure.messaging.eventhubs.eventprocessorclient.startstop
        eventProcessorClient.start();
        // Continue to perform other tasks while the processor is running in the background.
        eventProcessorClient.stop();
        // END: com.azure.messaging.eventhubs.eventprocessorclient.startstop
    }
}
