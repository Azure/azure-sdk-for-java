// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.PartitionContext;
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
     * @param eventData {@link EventData} received from this partition.
     * @return a representation of the deferred computation of this call.
     */
    @Override
    public Mono<Void> processEvent(PartitionContext partitionContext, EventData eventData) {
        logger.info(
            "Processing event: Event Hub name = {}; consumer group name = {}; partition id = {}; sequence number = {}",
            partitionContext.eventHubName(), partitionContext.consumerGroup(), partitionContext.partitionId(),
            eventData.sequenceNumber());
        return partitionContext.updateCheckpoint(eventData);
    }
}
