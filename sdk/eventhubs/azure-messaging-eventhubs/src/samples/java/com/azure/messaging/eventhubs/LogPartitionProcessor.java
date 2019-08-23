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
public class LogPartitionProcessor extends PartitionProcessor {

    private final Logger logger = LoggerFactory.getLogger(LogPartitionProcessor.class);

    /**
     * Creates a new {@link PartitionProcessor} instance that logs every interaction with {@link EventProcessor}.
     *
     * @param partitionContext The partition context to know which partition this processor is receiving events from.
     * @param checkpointManager The checkpoint manager for updating checkpoints.
     */
    public LogPartitionProcessor(PartitionContext partitionContext,
        CheckpointManager checkpointManager) {
        super(partitionContext, checkpointManager);
        logger.info("Creating partition processor: Event Hub name = {}; consumer group name = {}; partition id = {}",
            partitionContext.eventHubName(), partitionContext.consumerGroupName(), partitionContext.partitionId());
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
            partitionContext().eventHubName(), partitionContext().consumerGroupName(), partitionContext().partitionId(),
            eventData.sequenceNumber());
        return this.checkpointManager().updateCheckpoint(eventData);
    }
}
