/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.util.*;
import org.apache.qpid.proton.message.Message;
import com.microsoft.azure.servicebus.ReceiveHandler;

public abstract class PartitionReceiveHandler extends ReceiveHandler
{
	public abstract void onReceive(Iterable<EventData> events);
	
	@Override
	public void onReceiveMessages(LinkedList<Message> messages)
	{
		this.onReceive(EventDataUtil.toEventDataCollection(messages));
	}
}
