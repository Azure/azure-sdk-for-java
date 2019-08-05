// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.PartitionContext;
import reactor.core.publisher.Mono;

/**
 * Code snippets for {@link EventProcessor}.
 */
public final class EventProcessorJavaDocCodeSamples {

    /**
     * Code snippet for showing how to create a new instance of {@link EventProcessor}.
     *
     * @return An instance of {@link EventProcessor}.
     */
    public EventProcessor createInstance() {
        // BEGIN: com.azure.messaging.eventhubs.eventprocessor.instantiation
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";
        EventProcessor eventProcessor = new EventHubClientBuilder()
            .connectionString(connectionString)
            .partitionProcessorFactory((PartitionProcessorImpl::new))
            .consumerGroupName("consumer-group")
            .buildEventProcessor();
        // END: com.azure.messaging.eventhubs.eventprocessor.instantiation
        return eventProcessor;
    }

    /**
     * Code snippet for showing how to start and stop an {@link EventProcessor}.
     */
    public void startStopSample() {
        EventProcessor eventProcessor = createInstance();
        // BEGIN: com.azure.messaging.eventhubs.eventprocessor.startstop
        eventProcessor.start();
        // do other stuff while the event processor is running
        eventProcessor.stop();
        // END: com.azure.messaging.eventhubs.eventprocessor.startstop
    }

    /**
     * No-op partition processor used in code snippet to demo creating an instance of {@link EventProcessor}.
     * This class will not be visible in the code snippet.
     */
    private static final class PartitionProcessorImpl implements PartitionProcessor {

        private final ClientLogger logger = new ClientLogger(PartitionProcessorImpl.class);
        private final PartitionContext partitionContext;
        private final CheckpointManager checkpointManager;

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
            logger.info("Initializing partition processor for {}", this.partitionContext.partitionId());
            return Mono.empty();
        }

        /**
         * {@inheritDoc}
         *
         * @return a representation of deferred processing of events.
         */
        @Override
        public Mono<Void> processEvent(EventData eventData) {
            this.checkpointManager.updateCheckpoint(eventData);
            return Mono.empty();
        }

        /**
         * {@inheritDoc}
         *
         * @param throwable The {@link Throwable} that caused this method to be called.
         */
        @Override
        public void processError(Throwable throwable) {
            logger.warning("Error while processing events");
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
