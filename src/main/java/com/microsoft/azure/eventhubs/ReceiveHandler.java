package com.microsoft.azure.eventhubs;

import java.util.*;
import org.apache.qpid.proton.message.Message;
import com.microsoft.azure.servicebus.*;

public abstract class ReceiveHandler extends MessageReceiveHandler
{
	public abstract void onReceive(Collection<EventData> events);
	
	// TODO: return CompletableFuture<Void>
	@Override
	public void onReceiveMessages(Collection<Message> messages)
	{
		this.onReceive(EventDataUtil.toEventDataCollection(messages));
	}
}
