// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.eventprocessor;

import com.azure.messaging.eventhubs.CheckpointManager;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.eventprocessor.models.PartitionContext;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of a partition processor that logs the methods called in this implementation to a console.
 */
public class ConsolePartitionProcessor implements Subscriber<EventData> {

    private final Logger logger = LoggerFactory.getLogger(ConsolePartitionProcessor.class);
    private final PartitionContext partitionContext;
    private final CheckpointManager checkpointManager;
    private Subscription subscription;

    public ConsolePartitionProcessor(PartitionContext partitionContext,
        CheckpointManager checkpointManager) {
        this.partitionContext = partitionContext;
        this.checkpointManager = checkpointManager;
        logger.info("Initializing partition processor: event hub name = " + partitionContext
            .eventHubName() + "; consumer group name = " + partitionContext
            .consumerGroupName() + "; partition id = " + partitionContext.partitionId());
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        logger.info("Subscribing partition processor: event hub name = " + partitionContext
            .eventHubName() + "; consumer group name = " + partitionContext
            .consumerGroupName() + "; partition id = " + partitionContext.partitionId());
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(EventData eventData) {
        subscription.request(1);
        logger.info("Processing event with sequence number " + eventData.sequenceNumber()
            + " event hub name = " + partitionContext
            .eventHubName() + "; consumer group name = " + partitionContext
            .consumerGroupName() + "; partition id = " + partitionContext.partitionId());
        this.checkpointManager.updateCheckpoint(eventData);
    }

    @Override
    public void onError(Throwable throwable) {
        logger.warn("Error while processing event hub name = " + partitionContext
            .eventHubName() + "; consumer group name = " + partitionContext
            .consumerGroupName() + "; partition id = " + partitionContext.partitionId());
    }

    @Override
    public void onComplete() {
        logger.info(
            "Closing partition processor event hub name = " + partitionContext.eventHubName()
                + "; consumer group name = " + partitionContext.consumerGroupName()
                + "; partition id = " + partitionContext.partitionId());
        this.subscription.cancel();
        logger.info(
            "Closed partition processor event hub name = " + partitionContext.eventHubName()
                + "; consumer group name = " + partitionContext.consumerGroupName()
                + "; partition id = " + partitionContext.partitionId());
    }

}
