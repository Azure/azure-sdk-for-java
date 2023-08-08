// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.CloseReason;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
import java.util.function.Consumer;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;

/**
 * An abstract class defining all the operations that a partition processor can perform. Users of {@link
 * EventProcessorClient} should extend from this class and implement {@link #processEvent(EventContext)} for
 * processing events. Additionally, users can override:
 * <ul>
 *     <li>{@link #initialize(InitializationContext)} - This method is called before at the beginning of processing a
 *     partition.</li>
 *     <li>{@link #processError(ErrorContext)} - This method is called if there is an error while
 *     processing events</li>
 *     <li>{@link #close(CloseContext)} - This method is called at the end of processing a partition.
 *     The {@link CloseReason} specifies why the processing of a partition stopped.</li>
 * </ul>
 * <p>
 * An instance of partition processor will process events from a single partition only.
 * </p>
 * <p>Implementations of this abstract class also have the responsibility of updating checkpoints when appropriate.</p>
 */
public abstract class PartitionProcessor {

    private static final ClientLogger LOGGER = new ClientLogger(PartitionProcessor.class);

    /**
     * This method is called when this {@link EventProcessorClient} takes ownership of a new partition and before any
     * events from this partition are received.
     *
     * @param initializationContext The initialization context before events from the partition are processed.
     */
    public void initialize(InitializationContext initializationContext) {
        LOGGER.atInfo()
            .addKeyValue(PARTITION_ID_KEY, initializationContext.getPartitionContext().getPartitionId())
            .log("Initializing partition processor for partition");
    }

    /**
     * This method is called when a new event is received for this partition.
     *
     * @param eventContext The partition information and the next event data from this partition.
     */
    public abstract void processEvent(EventContext eventContext);

    /**
     * This method is called when a batch of events is received for this partition. To receive events in batches,
     * {@link EventProcessorClientBuilder#processEventBatch(Consumer, int) processEventBatch} has to be
     * setup when creating {@link EventProcessorClient} instance.
     *
     * @param eventBatchContext The event batch context containing the batch of events along with partition information.
     */
    public void processEventBatch(EventBatchContext eventBatchContext) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Processing event batch not implemented"));
    }

    /**
     * This method is called when an error occurs while receiving events from Event Hub. An error also marks the end of
     * event data stream.
     *
     * @param errorContext The error details and partition information where the error occurred.
     */
    public abstract void processError(ErrorContext errorContext);

    /**
     * This method is called before the partition processor is closed. A partition processor could be closed for various
     * reasons and the reasons and implementations of this interface can take appropriate actions to cleanup before the
     * partition processor is shutdown.
     *
     * @param closeContext Contains the reason for closing and the partition information for which the processing of
     * events is closed.
     */
    public void close(CloseContext closeContext) {
        LOGGER.atInfo()
            .addKeyValue(PARTITION_ID_KEY, closeContext.getPartitionContext().getPartitionId())
            .log("Closing partition processor with close reason {}", closeContext.getCloseReason());
    }
}
