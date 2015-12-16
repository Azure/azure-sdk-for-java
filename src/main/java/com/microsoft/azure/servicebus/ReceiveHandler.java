package com.microsoft.azure.servicebus;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.eventhubs.EventData;

public abstract class ReceiveHandler {
	
	public abstract void onReceive(Collection<EventData> events);
	
}
