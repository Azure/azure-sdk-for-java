// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
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
    public void processEvent(EventContext eventContext) {
        logger.info(
            "Processing event: Event Hub name = {}; consumer group name = {}; partition id = {}; sequence number = {}",
            eventContext.getPartitionContext().getEventHubName(),
            eventContext.getPartitionContext().getConsumerGroup(),
            eventContext.getPartitionContext().getPartitionId(),
            eventContext.getEventData().getSequenceNumber());
        eventContext.updateCheckpoint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processError(ErrorContext errorContext) {
        logger.warn("Error occurred in partition processor for partition {}",
            errorContext.getPartitionContext().getPartitionId(),
            errorContext.getThrowable());
    }
}
