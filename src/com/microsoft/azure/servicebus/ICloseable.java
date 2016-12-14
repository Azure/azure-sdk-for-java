package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public interface ICloseable {
	CompletableFuture<Void> closeAsync();
	
	void close() throws ServiceBusException;
}
