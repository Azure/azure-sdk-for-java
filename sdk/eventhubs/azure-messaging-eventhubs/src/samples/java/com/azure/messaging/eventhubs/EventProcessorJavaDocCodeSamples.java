// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import reactor.core.publisher.Mono;

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
        EventProcessor eventProcessor = new EventProcessorBuilder()
            .connectionString(connectionString)
            .processEvent(partitionEvent -> {
                System.out.println("Partition id = " + partitionEvent.getPartitionContext().getPartitionId() + " and "
                    + "sequence number of event = " + partitionEvent.getEventData().getSequenceNumber());
                return Mono.empty();
            })
            .consumerGroup("consumer-group")
            .buildEventProcessor();

        // BEGIN: com.azure.messaging.eventhubs.eventprocessor.startstop
        eventProcessor.start();
        // Continue to perform other tasks while the processor is running in the background.
        eventProcessor.stop();
        // END: com.azure.messaging.eventhubs.eventprocessor.startstop
    }
}
