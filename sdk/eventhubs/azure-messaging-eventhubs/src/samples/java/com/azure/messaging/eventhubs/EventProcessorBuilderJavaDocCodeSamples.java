// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import java.time.Duration;

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
            .checkpointStore(new SampleCheckpointStore())
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

    /**
     * Code snippet to show creation of an event processor that receives events in batches.
     */
    public void receiveBatchSample() {
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        // BEGIN: com.azure.messaging.eventhubs.eventprocessorclientbuilder.batchreceive
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup("consumer-group")
            .checkpointStore(new SampleCheckpointStore())
            .processEventBatch(eventBatchContext -> {
                eventBatchContext.getEvents().forEach(eventData -> {
                    System.out.println("Partition id = " + eventBatchContext.getPartitionContext().getPartitionId()
                        + "and sequence number of event = " + eventData.getSequenceNumber());
                });
            }, 50, Duration.ofSeconds(30))
            .processError(errorContext -> {
                System.out.printf("Error occurred in partition processor for partition {}, {}",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
            })
            .connectionString(connectionString)
            .buildEventProcessorClient();
        // END: com.azure.messaging.eventhubs.eventprocessorclientbuilder.batchreceive
    }

}
