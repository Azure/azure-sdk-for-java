// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.PartitionContext;
import reactor.core.publisher.Mono;

/**
 * Code snippets for {@link EventProcessorAsyncClient}.
 */
public final class EventProcessorJavaDocCodeSamples {

    /**
     * Code snippet for showing how to create a new instance of {@link EventProcessorAsyncClient}.
     *
     * @return An instance of {@link EventProcessorAsyncClient}.
     */
    public EventProcessorAsyncClient createInstance() {
        // BEGIN: com.azure.messaging.eventhubs.eventprocessorasyncclient.instantiation
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";
        EventProcessorAsyncClient eventProcessorAsyncClient = new EventHubClientBuilder()
            .connectionString(connectionString)
            .partitionProcessorFactory((PartitionProcessorImpl::new))
            .consumerGroupName("consumer-group")
            .buildEventProcessorAsyncClient();
        // END: com.azure.messaging.eventhubs.eventprocessorasyncclient.instantiation
        return eventProcessorAsyncClient;
    }

    /**
     * Code snippet for showing how to start and stop an {@link EventProcessorAsyncClient}.
     */
    public void startStopSample() {
        EventProcessorAsyncClient eventProcessorAsyncClient = createInstance();
        // BEGIN: com.azure.messaging.eventhubs.eventprocessorasyncclient.startstop
        eventProcessorAsyncClient.start();
        // do other stuff while the event processor is running
        eventProcessorAsyncClient.stop();
        // END: com.azure.messaging.eventhubs.eventprocessorasyncclient.startstop
    }

    /**
     * No-op partition processor used in code snippet to demo creating an instance of {@link EventProcessorAsyncClient}.
     * This class will not be visible in the code snippet.
     */
    private static final class PartitionProcessorImpl implements PartitionProcessor {

        PartitionContext partitionContext;
        CheckpointManager checkpointManager;

        /**
         * Creates new instance.
         *
         * @param partitionContext The partition context for this partition processor.
         * @param checkpointManager The checkpoint manager for this partition processor.
         */
        private PartitionProcessorImpl(PartitionContext partitionContext, CheckpointManager checkpointManager) {
            this.partitionContext = partitionContext;
            this.checkpointManager = checkpointManager;
        }

        /**
         * {@inheritDoc}
         *
         * @return a representation of deferred initialization.
         */
        @Override
        public Mono<Void> initialize() {
            return Mono.empty();
        }

        /**
         * {@inheritDoc}
         *
         * @return a representation of deferred processing of events.
         */
        @Override
        public Mono<Void> processEvent(EventData eventData) {
            return Mono.empty();
        }

        /**
         * {@inheritDoc}
         *
         * @param throwable The {@link Throwable} that caused this method to be called.
         */
        @Override
        public void processError(Throwable throwable) {
            System.out.println("Error while processing events");
        }

        /**
         * {@inheritDoc}
         *
         * @param closeReason {@link CloseReason} for closing this partition processor.
         * @return a representation of deferred closing of partition processor.
         */
        @Override
        public Mono<Void> close(CloseReason closeReason) {
            return Mono.empty();
        }
    }

}
