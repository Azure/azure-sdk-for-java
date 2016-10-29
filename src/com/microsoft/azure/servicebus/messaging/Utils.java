package com.microsoft.azure.servicebus.messaging;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.servicebus.ServiceBusException;

class Utils {
	static <T> T completeFuture(CompletableFuture<T> future) throws InterruptedException, ServiceBusException
	{
		try
		{
			return future.get();
		}
		catch(InterruptedException ie)
		{
			Thread.currentThread().interrupt();
			throw ie;
		}
		catch(ExecutionException ee)
		{
			Throwable cause = ee.getCause();
			if(cause instanceof ServiceBusException)
			{
				throw (ServiceBusException)cause;
			}
			else
			{
				throw new ServiceBusException(true, cause);
			}			
		}		
	}
	
	static void assertNonNull(String argumentName, Object argument)
	{
		if(argument == null)
			throw new IllegalArgumentException("Argument '" + argumentName +"' is null.");
	}
}
