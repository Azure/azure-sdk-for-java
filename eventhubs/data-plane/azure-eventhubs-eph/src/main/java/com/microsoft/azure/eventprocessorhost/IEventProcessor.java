// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.EventData;


/**
 * Interface that must be implemented by event processor classes.
 * <p>
 * Any given instance of an event processor class will only process events from one partition
 * of one Event Hub. A PartitionContext is provided with each call to the event processor because
 * some parameters could change, but it will always be the same partition.
 * <p>
 * Although EventProcessorHost is multithreaded, calls to a given instance of an event processor
 * class are serialized, except for onError(). onOpen() is called first, then onEvents() will be called zero or more
 * times. When the event processor needs to be shut down, whether because there was a failure
 * somewhere, or the lease for the partition has been lost, or because the entire processor host
 * is being shut down, onClose() is called after the last onEvents() call returns.
 * <p>
 * onError() could be called while onEvents() or onClose() is executing. No synchronization is attempted
 * in order to avoid possibly deadlocking.
 */
public interface IEventProcessor {
    /**
     * Called by processor host to initialize the event processor.
     * <p>
     * If onOpen fails, this event processor host instance will give up ownership of the partition.
     *
     * @param context Information about the partition that this event processor will process events from.
     * @throws Exception to indicate failure.
     */
    void onOpen(PartitionContext context) throws Exception;

    /**
     * Called by processor host to indicate that the event processor is being stopped.
     * <p>
     * If onClose fails, the exception is reported to the general exception notification handler set via
     * EventProcessorOptions, if any, but is otherwise ignored.
     *
     * @param context Information about the partition.
     * @param reason  Reason why the event processor is being stopped.
     * @throws Exception to indicate failure.
     */
    void onClose(PartitionContext context, CloseReason reason) throws Exception;

    /**
     * Called by the processor host when a batch of events has arrived.
     * <p>
     * This is where the real work of the event processor is done. It is normally called when one
     * or more events have arrived. If the EventProcessorHost instance was set up with an EventProcessorOptions
     * on which setInvokeProcessorAfterReceiveTimeout(true) has been called, then when a receive times out,
     * onEvents will be called with an empty iterable. By default this option is false and receive timeouts do not
     * cause a call to this method.
     *
     * @param context Information about the partition.
     * @param events  The events to be processed. May be empty.
     * @throws Exception to indicate failure.
     */
    void onEvents(PartitionContext context, Iterable<EventData> events) throws Exception;

    /**
     * Called when the underlying client experiences an error while receiving. EventProcessorHost will take
     * care of recovering from the error and continuing to pump events, so no action is required from
     * your code. This method is provided for informational purposes.
     *
     * @param context Information about the partition.
     * @param error   The error that occured.
     */
    void onError(PartitionContext context, Throwable error);
}
