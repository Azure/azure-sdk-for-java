package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

public interface ISessionHandler
{
	public CompletableFuture<Void> onMessageAsync(IMessageSession session, IMessage message);
	
	/**
	 * Called just before a session is closed by the session pump
	 * @param session session being closed
	 * @return a future that executes the action
	 */
	public CompletableFuture<Void> OnCloseSessionAsync(IMessageSession session);
	
	public void notifyException(Throwable exception, ExceptionPhase phase);
}
