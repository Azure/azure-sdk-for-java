// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.PartitionContext;
import reactor.core.publisher.Mono;

/**
 * An abstract class defining all the operations that a partition processor can perform. Users of {@link EventProcessor}
 * should extend from this class and implement {@link #processEvent(PartitionContext, EventData)} for processing events.
 * Additionally, users can override:
 * <ul>
 *     <li>{@link #initialize(PartitionContext)} - This method is called before at the beginning of processing a
 *     partition.</li>
 *     <li>{@link #processError(PartitionContext, Throwable)} - This method is called if there is an error while
 *     processing events</li>
 *     <li>{@link #close(PartitionContext, CloseReason)} - This method is called at the end of processing a partition.
 *     The {@link CloseReason} specifies why the processing of a partition stopped.</li>
 * </ul>
 * <p>
 * An instance of partition processor will process events from a single partition only.
 * </p>
 * <p>Implementations of this abstract class also have the responsibility of updating checkpoints when appropriate.</p>
 */
public abstract class PartitionProcessor {

    private final ClientLogger logger = new ClientLogger(PartitionProcessor.class);

    /**
     * This method is called when this {@link EventProcessor} takes ownership of a new partition and before any events
     * from this partition are received.
     *
     * @param partitionContext The partition information for initialization before events from the partition are
     * processed.
     * @return a representation of the deferred computation of this call.
     */
    public Mono<Void> initialize(PartitionContext partitionContext) {
        logger.info("Initializing partition processor for partition {}", partitionContext.partitionId());
        return Mono.empty();
    }

    /**
     * This method is called when a new event is received for this partition. Processing of this event can happen
     * asynchronously.
     *
     * <p>
     * This is also a good place to update checkpoints using the {@code partitionContext} as appropriate.
     *
     * @param partitionContext The partition information the event data belongs to.
     * @param eventData {@link EventData} received from this partition.
     * @return a representation of the deferred computation of this call.
     */
    public abstract Mono<Void> processEvent(PartitionContext partitionContext, EventData eventData);

    /**
     * This method is called when an error occurs while receiving events from Event Hub. An error also marks the end of
     * event data stream.
     *
     * @param partitionContext The partition information where the error occurred.
     * @param throwable The {@link Throwable} that caused this method to be called.
     */
    public void processError(PartitionContext partitionContext, Throwable throwable) {
        logger.warning("Error occurred in partition processor for partition {}", partitionContext.partitionId(),
            throwable);
    }

    /**
     * This method is called before the partition processor is closed. A partition processor could be closed for various
     * reasons and the reasons and implementations of this interface can take appropriate actions to cleanup before the
     * partition processor is shutdown.
     *
     * @param partitionContext The partition information for which the processing of events is closed.
     * @param closeReason The reason for closing this partition processor.
     * @return a representation of the deferred computation of this call.
     */
    public Mono<Void> close(PartitionContext partitionContext, CloseReason closeReason) {
        logger.info("Closing partition processor for partition {} with close reason {}",
            partitionContext.partitionId(), closeReason);
        return Mono.empty();
    }

}
