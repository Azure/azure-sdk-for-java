// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.PartitionContext;
import java.util.function.Function;
import reactor.core.publisher.Mono;

public class EventProcessorBuilderJavaDocCodeSamples {

    /**
     * Code snippet for showing how to create a new instance of {@link EventProcessor} using the {@link
     * EventProcessorBuilder#processEvent(Function)} lambda function.
     */
    public void createInstanceUsingLambdaFunction() {
        // BEGIN: com.azure.messaging.eventhubs.eventprocessorbuilder.processevent
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";
        EventHubAsyncClient eventHubAsyncClient = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        EventProcessor eventProcessor = new EventProcessorBuilder()
            .consumerGroup("consumer-group")
            .eventHubClient(eventHubAsyncClient)
            .processEvent(eventData -> Mono.fromRunnable(() -> System.out.println(eventData.bodyAsString())))
            .buildEventProcessor();
        // END: com.azure.messaging.eventhubs.eventprocessorbuilder.processevent
    }

    /**
     * Code snippet for showing how to create a new instance of {@link EventProcessor} using the {@link
     * PartitionProcessorFactory}.
     *
     * @return A new instance of {@link EventProcessor}
     */
    // BEGIN: com.azure.messaging.eventhubs.eventprocessorbuilder.partitionprocessorfactory
    public EventProcessor createEventProcessor() {
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";
        EventHubAsyncClient eventHubAsyncClient = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        EventProcessor eventProcessor = new EventProcessorBuilder()
            .consumerGroup("consumer-group")
            .eventHubClient(eventHubAsyncClient)
            .partitionProcessorFactory((PartitionProcessorImpl::new))
            .buildEventProcessor();
        return eventProcessor;
    }

    /**
     * A partition processor to demo creating an instance of {@link EventProcessor}.
     */
    public static final class PartitionProcessorImpl extends PartitionProcessor {

        /**
         * Creates a new instance of {@link PartitionProcessorImpl}.
         *
         * @param partitionContext The partition information specific to this instance of {@link
         * PartitionProcessorImpl}.
         * @param checkpointManager The checkpoint manager used for updating checkpoints.
         */
        public PartitionProcessorImpl(PartitionContext partitionContext, CheckpointManager checkpointManager) {
            super(partitionContext, checkpointManager);
        }

        /**
         * Processes the event data.
         *
         * @return a representation of deferred processing of events.
         */
        @Override
        public Mono<Void> processEvent(EventData eventData) {
            System.out.println("Processing event with sequence number " + eventData.sequenceNumber());
            return checkpointManager().updateCheckpoint(eventData);
        }
    }
    // END: com.azure.messaging.eventhubs.eventprocessorbuilder.partitionprocessorfactory

}
