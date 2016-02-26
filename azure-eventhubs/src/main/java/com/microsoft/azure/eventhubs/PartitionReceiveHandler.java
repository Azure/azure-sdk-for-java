/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.util.*;
import org.apache.qpid.proton.message.Message;
import com.microsoft.azure.servicebus.ReceiveHandler;

/**
 * A handler class for the receive operation. Use any implementation of this abstract class to specify 
 * user action when using PartitionReceiver's setReceiveHandler().
 * @see  {@link PartitionReceiver#setReceiveHandler}
 */
public abstract class PartitionReceiveHandler extends ReceiveHandler
{
    /**
     * user should implement this method to specify the action to be performed on the received events.
     * @param   events  the list of fetched events from the corresponding PartitionReceiver.
     * @see  {@link PartitionReceiver#receive}
     */
	public abstract void onReceive(Iterable<EventData> events);
	
    /**
     * Generic version of onReceive. This method internally call the type specific version onReceive() instead.
     * @param   messages  the list of fetched messages from the underlying protocol layer.
     * @see  {@link PartitionReceiveHandler#onReceive}
     */
	@Override
	public void onReceiveMessages(LinkedList<Message> messages)
	{
		this.onReceive(EventDataUtil.toEventDataCollection(messages));
	}
}
