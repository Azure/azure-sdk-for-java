// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.PartitionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * A sample implementation of {@link PartitionProcessor}. This implementation logs the APIs that were called by {@link
 * EventProcessor} while processing a partition.
 */
public class LogPartitionProcessor implements PartitionProcessor {

    private final Logger logger = LoggerFactory.getLogger(LogPartitionProcessor.class);
    private final PartitionContext partitionContext;
    private final CheckpointManager checkpointManager;

    /**
     * Creates a new {@link PartitionProcessor} instance that logs every interaction with {@link
     * EventProcessor}.
     *
     * @param partitionContext The partition context to know which partition this processor is receiving events from.
     * @param checkpointManager The checkpoint manager for updating checkpoints.
     */
    public LogPartitionProcessor(PartitionContext partitionContext,
        CheckpointManager checkpointManager) {
        this.partitionContext = partitionContext;
        this.checkpointManager = checkpointManager;
        logger.info("Creating partition processor: Event Hub name = {}; consumer group name = {}; partition id = {}",
            partitionContext.eventHubName(), partitionContext.consumerGroupName(), partitionContext.partitionId());
    }

    /**
     * {@inheritDoc}
     *
     * @return a representation of the deferred computation of this call.
     */
    @Override
    public Mono<Void> initialize() {
        logger
            .info("Initializing partition processor: Event Hub name = {}; consumer group name = {}; partition id = {}",
                partitionContext.eventHubName(), partitionContext.consumerGroupName(), partitionContext.partitionId());
        return Mono.empty();
    }

    /**
     * {@inheritDoc}
     *
     * @param eventData {@link EventData} received from this partition.
     * @return a representation of the deferred computation of this call.
     */
    @Override
    public Mono<Void> processEvent(EventData eventData) {
        logger.info(
            "Processing event: Event Hub name = {}; consumer group name = {}; partition id = {}; sequence number = {}",
            partitionContext.eventHubName(), partitionContext.consumerGroupName(), partitionContext.partitionId(),
            eventData.sequenceNumber());
        return this.checkpointManager.updateCheckpoint(eventData);
    }

    /**
     * {@inheritDoc}
     *
     * @param throwable The {@link Throwable} that caused this method to be called.
     */
    @Override
    public void processError(Throwable throwable) {
        logger
            .warn("Processing error: Event Hub name = {}; consumer group name = {}; partition id = {}; throwable = {}",
                partitionContext.eventHubName(), partitionContext.consumerGroupName(), partitionContext.partitionId(),
                throwable.getMessage());
    }

    /**
     * {@inheritDoc}
     *
     * @param closeReason The reason for closing this partition processor.
     * @return a representation of the deferred computation of this call.
     */
    @Override
    public Mono<Void> close(CloseReason closeReason) {
        logger.info(
            "Closing partition processor: Event Hub name = {}; consumer group name = {}; partition id = {}; closeReason = {}",
            partitionContext.eventHubName(), partitionContext.consumerGroupName(), partitionContext.partitionId(),
            closeReason);
        return Mono.empty();
    }
}
