// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.models.EventProcessingErrorContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sample implementation of {@link PartitionProcessor}. This implementation logs the APIs that are called by {@link
 * EventProcessorClient} while processing a partition.
 */
public class LogPartitionProcessor extends PartitionProcessor {

    private final Logger logger = LoggerFactory.getLogger(LogPartitionProcessor.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void processEvent(PartitionEvent partitionEvent) {
        logger.info(
            "Processing event: Event Hub name = {}; consumer group name = {}; partition id = {}; sequence number = {}",
            partitionEvent.getPartitionContext().getEventHubName(),
            partitionEvent.getPartitionContext().getConsumerGroup(),
            partitionEvent.getPartitionContext().getPartitionId(),
            partitionEvent.getData().getSequenceNumber());
        partitionEvent.getPartitionContext().updateCheckpoint(partitionEvent.getData()).subscribe();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processError(EventProcessingErrorContext eventProcessingErrorContext) {
        logger.warn("Error occurred in partition processor for partition {}",
            eventProcessingErrorContext.getPartitionContext().getPartitionId(),
            eventProcessingErrorContext.getThrowable());
    }
}
