package com.microsoft.azure.servicebus.messaging;

import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.ServiceBusException;

public interface ICloseable {
	CompletableFuture<Void> closeAsync();
	
	void close() throws ServiceBusException;
}
