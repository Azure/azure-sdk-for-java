/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

/**
 * A handler class for the receive operation. Use any implementation of this abstract class to specify 
 * user action when using PartitionReceiver's setReceiveHandler().
 * @see  {@link PartitionReceiver#setReceiveHandler}
 */
public abstract class PartitionReceiveHandler
{
    /**
     * user should implement this method to specify the action to be performed on the received events.
     * @param   events  the list of fetched events from the corresponding PartitionReceiver.
     * @see  {@link PartitionReceiver#receive}
     */
	public abstract void onReceive(Iterable<EventData> events);
	
	public abstract void onError(Throwable error);
	
	public abstract void onClose(Throwable error);
}
