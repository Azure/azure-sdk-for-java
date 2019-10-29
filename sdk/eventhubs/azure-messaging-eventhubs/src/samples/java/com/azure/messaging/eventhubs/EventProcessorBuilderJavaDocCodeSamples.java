// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.PartitionContext;
import reactor.core.publisher.Mono;

/**
 * Code snippets for {@link EventProcessorBuilder}.
 */
public class EventProcessorBuilderJavaDocCodeSamples {

    /**
     * Code snippet for showing how to create a new instance of {@link EventProcessor}.
     *
     * @return A new instance of {@link EventProcessor}
     */
    // BEGIN: com.azure.messaging.eventhubs.eventprocessorbuilder.instantiation
    public EventProcessor createEventProcessor() {
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";
        EventHubConnection eventHubAsyncClient = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildConnection();

        EventProcessor eventProcessor = new EventProcessorBuilder()
            .consumerGroup("consumer-group")
            .eventHubClient(eventHubAsyncClient)
            .partitionProcessorFactory((PartitionProcessorImpl::new))
            .partitionManager(new InMemoryPartitionManager())
            .buildEventProcessor();
        return eventProcessor;
    }

    /**
     * A partition processor to demo creating an instance of {@link EventProcessor}.
     */
    public static final class PartitionProcessorImpl extends PartitionProcessor {

        /**
         * Processes the event data.
         *
         * @return a representation of deferred processing of events.
         */
        @Override
        public Mono<Void> processEvent(PartitionContext partitionContext, EventData eventData) {
            System.out.println("Processing event with sequence number " + eventData.getSequenceNumber());
            return partitionContext.updateCheckpoint(eventData);
        }
    }
    // END: com.azure.messaging.eventhubs.eventprocessorbuilder.instantiation

}
