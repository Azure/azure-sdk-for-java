/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

/**
 * A handler class for the receive operation. Use any implementation of this abstract class to specify 
 * user action when using PartitionReceiver's setReceiveHandler().
 * @see  PartitionReceiver#setReceiveHandler
 */
public abstract class PartitionReceiveHandler
{
	private int maxEventCount;

	protected PartitionReceiveHandler(final int maxEventCount)
	{
		this.maxEventCount = maxEventCount;
	}

	int getMaxEventCount()
	{
		return maxEventCount;
	}

	/**
	 * implementor of {@link PartitionReceiveHandler#onReceive} can use this to set the limit on maximum {@link EventData}'s that
	 * can be received by the next {@link PartitionReceiveHandler#onReceive} call
	 * @param value maximum {@link EventData}'s to be received in the next {@link PartitionReceiveHandler#onReceive} call
	 */
	protected final void setMaxEventCount(final int value)
	{
		this.maxEventCount = value;
	}

	/**
	 * user should implement this method to specify the action to be performed on the received events.
	 * @param   events  the list of fetched events from the corresponding PartitionReceiver.
	 * @see  PartitionReceiver#receive
	 */
	public abstract void onReceive(Iterable<EventData> events);

	/**
	 * Implement this method to Listen to errors which lead to Closure of the {@link PartitionReceiveHandler} pump.
	 * @param error fatal error encountered while running the {@link PartitionReceiveHandler} pump
	 */
	public abstract void onError(Throwable error);
}