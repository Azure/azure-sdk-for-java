package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

public interface IMessageHandler
{
	public CompletableFuture<Void> onMessageAsync(IMessage message);
	
	public void notifyException(Throwable exception, ExceptionPhase phase);
}
