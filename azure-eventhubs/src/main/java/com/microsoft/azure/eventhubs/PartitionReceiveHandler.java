package com.microsoft.azure.eventhubs;

import java.util.*;
import org.apache.qpid.proton.message.Message;
import com.microsoft.azure.servicebus.ReceiveHandler;

public abstract class PartitionReceiveHandler extends ReceiveHandler
{
	public abstract void onReceive(Iterable<EventData> events);
	
	// TODO: Add OnError functionality
	// TODO: return CompletableFuture<Void>
	@Override
	public void onReceiveMessages(LinkedList<Message> messages)
	{
		this.onReceive(EventDataUtil.toEventDataCollection(messages));
	}
}
