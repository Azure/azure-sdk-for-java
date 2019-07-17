// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;


/**
 * Interface that must be implemented by an event processor factory class.
 * <p>
 * User-provided factories are needed if creating an event processor object requires more work than
 * just a new with a parameterless constructor.
 *
 * @param <T> The type of event processor objects produced by this factory, which must implement IEventProcessor
 */
public interface IEventProcessorFactory<T extends IEventProcessor> {
    /**
     * Called to create an event processor for the given partition.
     * <p>
     * If it throws an exception, that causes this event processor host instance to give up ownership of the partition.
     *
     * @param context Information about the partition that the event processor will handle events from.
     * @throws Exception to indicate failure.
     * @return The event processor object.
     */
    T createEventProcessor(PartitionContext context) throws Exception;
}
