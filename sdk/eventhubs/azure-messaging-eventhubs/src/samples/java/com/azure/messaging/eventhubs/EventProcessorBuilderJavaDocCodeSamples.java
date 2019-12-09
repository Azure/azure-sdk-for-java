// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

/**
 * Code snippets for {@link EventProcessorClientBuilder}.
 */
public class EventProcessorBuilderJavaDocCodeSamples {

    /**
     * Code snippet for showing how to create a new instance of {@link EventProcessorClient}.
     *
     * @return A new instance of {@link EventProcessorClient}
     */
    // BEGIN: com.azure.messaging.eventhubs.eventprocessorclientbuilder.instantiation
    public EventProcessorClient createEventProcessor() {
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup("consumer-group")
            .checkpointStore(new InMemoryCheckpointStore())
            .processEvent(eventContext -> {
                System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId()
                    + "and sequence number of event = " + eventContext.getEventData().getSequenceNumber());
            })
            .processError(errorContext -> {
                System.out.printf("Error occurred in partition processor for partition {}, {}",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
            })
            .connectionString(connectionString)
            .buildEventProcessorClient();
        return eventProcessorClient;
    }
    // END: com.azure.messaging.eventhubs.eventprocessorclientbuilder.instantiation

}
