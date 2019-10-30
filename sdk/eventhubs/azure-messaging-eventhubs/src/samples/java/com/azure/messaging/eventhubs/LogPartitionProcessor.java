// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * A sample implementation of {@link PartitionProcessor}. This implementation logs the APIs that are called by {@link
 * EventProcessor} while processing a partition.
 */
public class LogPartitionProcessor extends PartitionProcessor {

    private final Logger logger = LoggerFactory.getLogger(LogPartitionProcessor.class);

    /**
     * {@inheritDoc}
     *
     * @param partitionEvent {@link EventData} and the partition information associated with this event.
     * @return a representation of the deferred computation of this call.
     */
    @Override
    public Mono<Void> processEvent(PartitionEvent partitionEvent) {
        logger.info(
            "Processing event: Event Hub name = {}; consumer group name = {}; partition id = {}; sequence number = {}",
            partitionEvent.getPartitionContext().getEventHubName(),
            partitionEvent.getPartitionContext().getConsumerGroup(),
            partitionEvent.getPartitionContext().getPartitionId(),
            partitionEvent.getEventData().getSequenceNumber());
        return partitionEvent.getPartitionContext().updateCheckpoint(partitionEvent.getEventData());
    }
}
