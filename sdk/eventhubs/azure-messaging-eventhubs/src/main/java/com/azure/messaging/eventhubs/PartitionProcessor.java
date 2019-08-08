// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.PartitionContext;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the operations that must be supported by a single partition processor.
 * <p>
 * An instance of partition processor will process events only from a single partition. New instances of partition
 * processors will be created through {@link PartitionProcessorFactory#createPartitionProcessor(PartitionContext,
 * CheckpointManager) PartitionProcessorFactory}.
 * </p>
 * <p>
 * Implementations of this interface also have the responsibility of updating checkpoints when appropriate.
 * </p>
 */
public interface PartitionProcessor {

    /**
     * This method is called when this {@link EventProcessor} takes ownership of a new partition and before any
     * events from this partition are received.
     *
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> initialize();

    /**
     * This method is called when a new event is received for this partition. Processing of this event can happen
     * asynchronously.
     *
     * <p>
     * This is also a good place to update checkpoints as appropriate.
     *
     * @param eventData {@link EventData} received from this partition.
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> processEvent(EventData eventData);

    /**
     * This method is called when an error occurs while receiving events from Event Hub. An error also marks the end of
     * event data stream.
     *
     * @param throwable The {@link Throwable} that caused this method to be called.
     */
    void processError(Throwable throwable);

    /**
     * This method is called before the partition processor is closed. A partition processor could be closed for various
     * reasons and the reasons and implementations of this interface can take appropriate actions to cleanup before the
     * partition processor is shutdown.
     *
     * @param closeReason The reason for closing this partition processor.
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> close(CloseReason closeReason);

}
