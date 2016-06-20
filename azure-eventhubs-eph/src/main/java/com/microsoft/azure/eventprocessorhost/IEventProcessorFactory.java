/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;


/**
 * Interface that must be implemented by an event processor factory class.
 * 
 * User-provided factories are needed if creating an event processor object requires more work than
 * just a new with a parameterless constructor.
 *
 * @param <T>	The type of event processor objects produced by this factory, which must implement IEventProcessor
 */
public interface IEventProcessorFactory<T extends IEventProcessor>
{
	/**
	 * Called to create an event processor for the given partition.
	 * 
	 * @param context	Information about the partition that the event processor will handle events from.
	 * @return			The event processor object.
	 * @throws Exception
	 */
    public T createEventProcessor(PartitionContext context) throws Exception;
}
