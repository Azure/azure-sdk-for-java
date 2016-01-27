package com.microsoft.azure.servicebus;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.eventhubs.EventData;

public abstract class MessageReceiveHandler
{
	
	public abstract void onReceiveMessages(Collection<Message> messages);
	
}
