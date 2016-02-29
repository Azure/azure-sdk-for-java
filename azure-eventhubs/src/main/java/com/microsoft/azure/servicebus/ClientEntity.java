/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *  Contract for all client entities with Open-Close/Abort state m/c
 *  main-purpose: closeAll related entities
 *  Internal-class
 */
public abstract class ClientEntity
{
	private String clientId;
	protected ClientEntity(final String clientId)
	{
		this.clientId = clientId;
	}
	
	public abstract CompletableFuture<Void> close();
	
	public void closeSync() throws ServiceBusException
	{
		try
		{
			this.close().get();
		}
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}
	}
	
	public String getClientId()
	{
		return this.clientId;
	}
}
