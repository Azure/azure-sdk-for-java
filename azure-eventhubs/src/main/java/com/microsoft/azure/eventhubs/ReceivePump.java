/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.azure.servicebus.ClientConstants;
import com.microsoft.azure.servicebus.ServiceBusException;

public class ReceivePump
{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	
	private final IPartitionReceiver receiver;
	private final PartitionReceiveHandler onReceiveHandler;
	private final boolean invokeOnTimeout;
	private final CompletableFuture<Void> stopPump;

	private AtomicBoolean stopPumpRaised;
	
	public ReceivePump(
			final IPartitionReceiver receiver,
			final PartitionReceiveHandler receiveHandler,
			final boolean invokeOnReceiveWithNoEvents)
	{
		this.receiver = receiver;
		this.onReceiveHandler = receiveHandler;
		this.invokeOnTimeout = invokeOnReceiveWithNoEvents;
		this.stopPump = new CompletableFuture<Void>();
		
		this.stopPumpRaised = new AtomicBoolean(false);
	}	
	
	public void run()
	{
		boolean isPumpHealthy = true;
		while(isPumpHealthy && !this.stopPumpRaised.get())
		{
			Iterable<EventData> receivedEvents = null;

			try
			{
				receivedEvents = this.receiver.receive(this.onReceiveHandler.getMaxEventCount());
			}
			catch (Throwable clientException)
			{
				isPumpHealthy = false;
				this.onReceiveHandler.onError(clientException);
				
				if (TRACE_LOGGER.isLoggable(Level.WARNING))
				{
					TRACE_LOGGER.log(Level.WARNING, String.format("Receive pump for partition (%s) exiting after receive exception %s", this.receiver.getPartitionId(), clientException.toString()));
				}
			}

			try
			{
				if (receivedEvents != null || (receivedEvents == null && this.invokeOnTimeout && isPumpHealthy))
				{
					this.onReceiveHandler.onReceive(receivedEvents);	
				}
			}
			catch (Throwable userCodeError)
			{
				isPumpHealthy = false;
				this.onReceiveHandler.onError(userCodeError);

				if (userCodeError instanceof InterruptedException)
				{
					if(TRACE_LOGGER.isLoggable(Level.FINE))
					{
						TRACE_LOGGER.log(Level.FINE, String.format("Interrupting receive pump for partition (%s)", this.receiver.getPartitionId()));
					}
					
					Thread.currentThread().interrupt();
				}
				else if (TRACE_LOGGER.isLoggable(Level.SEVERE))
				{
					TRACE_LOGGER.log(Level.SEVERE, String.format("Receive pump for partition (%s) exiting after user exception %s", this.receiver.getPartitionId(), userCodeError.toString()));
				}
			}
		}
		
		this.stopPump.complete(null);
	}
	
	public CompletableFuture<Void> stop()
	{
		this.stopPumpRaised.set(true);
		return this.stopPump;
	}
	
	public boolean isRunning()
	{
		return !this.stopPump.isDone();
	}

	// partition receiver contract against which this pump works
	public static interface IPartitionReceiver
	{
		public String getPartitionId();

		public Iterable<EventData> receive(final int maxBatchSize) throws ServiceBusException;
	}
}
