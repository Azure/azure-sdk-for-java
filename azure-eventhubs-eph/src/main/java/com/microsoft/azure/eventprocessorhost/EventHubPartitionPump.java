/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.servicebus.ReceiverDisconnectedException;
import com.microsoft.azure.servicebus.ServiceBusException;

class EventHubPartitionPump extends PartitionPump
{
    private CompletableFuture<?> internalOperationFuture = null;
    
	private EventHubClient eventHubClient = null;
	private PartitionReceiver partitionReceiver = null;
    private InternalReceiveHandler internalReceiveHandler = null;

	EventHubPartitionPump(EventProcessorHost host, Lease lease)
	{
		super(host, lease);
	}

    @Override
    void specializedStartPump()
    {
    	boolean openedOK = false;
    	int retryCount = 0;
    	Exception lastException = null;
    	do
    	{
	        try
	        {
				openClients();
				openedOK = true;
			}
	        catch (Exception e)
	        {
	        	lastException = e;
	        	if ((e instanceof ExecutionException) && (e.getCause() instanceof ReceiverDisconnectedException))
	        	{
	        		// TODO Assuming this is due to a receiver with a higher epoch.
	        		// Is there a way to be sure without checking the exception text?
	        		this.host.logWithHostAndPartition(Level.WARNING, this.partitionContext, "Receiver disconnected on create, bad epoch?", e);
	        		// If it's a bad epoch, then retrying isn't going to help.
	        		break;
	        	}
	        	else
	        	{
					this.host.logWithHostAndPartition(Level.WARNING, this.partitionContext, "Failure creating client or receiver, retrying", e);
					retryCount++;
	        	}
			}
    	} while (!openedOK && (retryCount < 5));
    	if (!openedOK)
    	{
            // IEventProcessor.onOpen is called from the base PartitionPump and must have returned in order for execution to reach here, 
    		// so we can report this error to it instead of the general error handler.
    		this.processor.onError(this.partitionContext, lastException);
			this.pumpStatus = PartitionPumpStatus.PP_OPENFAILED;
    	}

        if (this.pumpStatus == PartitionPumpStatus.PP_OPENING)
        {
            this.internalReceiveHandler = new InternalReceiveHandler();
            // IEventProcessor.onOpen is called from the base PartitionPump and must have returned in order for execution to reach here, 
            // meaning it is safe to set the handler and start calling IEventProcessor.onEvents.
            // Set the status to running before setting the javaClient handler, so the IEventProcessor.onEvents can never race and see status != running.
            this.pumpStatus = PartitionPumpStatus.PP_RUNNING;
            this.partitionReceiver.setReceiveHandler(this.internalReceiveHandler);
        }
        
        if (this.pumpStatus == PartitionPumpStatus.PP_OPENFAILED)
        {
        	this.pumpStatus = PartitionPumpStatus.PP_CLOSING;
        	cleanUpClients();
        	this.pumpStatus = PartitionPumpStatus.PP_CLOSED;
        }
    }
    
    private void openClients() throws ServiceBusException, IOException, InterruptedException, ExecutionException
    {
    	// Create new client
    	this.host.logWithHostAndPartition(Level.FINE, this.partitionContext, "Opening EH client");
		this.internalOperationFuture = EventHubClient.createFromConnectionString(this.host.getEventHubConnectionString());
		this.eventHubClient = (EventHubClient) this.internalOperationFuture.get();
		this.internalOperationFuture = null;
		
		// Create new receiver and set options
    	Object startAt = this.partitionContext.getInitialOffset();
    	long epoch = this.lease.getEpoch();
    	this.host.logWithHostAndPartition(Level.FINE, this.partitionContext, "Opening EH receiver with epoch " + epoch + " at location " + startAt);
    	if (startAt instanceof String)
    	{
    		this.internalOperationFuture = this.eventHubClient.createEpochReceiver(this.partitionContext.getConsumerGroupName(), this.partitionContext.getPartitionId(),
    				(String)startAt, epoch);
    	}
    	else if (startAt instanceof Instant) 
    	{
    		this.internalOperationFuture = this.eventHubClient.createEpochReceiver(this.partitionContext.getConsumerGroupName(), this.partitionContext.getPartitionId(),
    				(Instant)startAt, epoch);
    	}
    	// else getInitialOffset threw an exception and we never get here
		this.lease.setEpoch(epoch);
		this.partitionReceiver = (PartitionReceiver) this.internalOperationFuture.get();
		this.partitionReceiver.setPrefetchCount(this.host.getEventProcessorOptions().getPrefetchCount());
		this.partitionReceiver.setReceiveTimeout(this.host.getEventProcessorOptions().getReceiveTimeOut());
		this.internalOperationFuture = null;
		
        this.host.logWithHostAndPartition(Level.FINE, this.partitionContext, "EH client and receiver creation finished");
    }
    
    private void cleanUpClients() // swallows all exceptions
    {
        if (this.partitionReceiver != null)
        {
    		// Taking the lock means that there is no onEvents call in progress.
        	synchronized (this.processingSynchronizer)
        	{
        		// Disconnect the processor from the receiver we're about to close.
        		// Fortunately this is idempotent -- setting the handler to null when it's already been
        		// nulled by code elsewhere is harmless!
        		this.partitionReceiver.setReceiveHandler(null);
        	}
        	
        	this.host.logWithHostAndPartition(Level.FINE, this.partitionContext, "Closing EH receiver");
        	this.partitionReceiver.close();
        	this.partitionReceiver = null;
        }
        
        if (this.eventHubClient != null)
        {
        	this.host.logWithHostAndPartition(Level.FINE, this.partitionContext, "Closing EH client");
        	this.eventHubClient.close();
        	this.eventHubClient = null;
        }
    }

    @Override
    void specializedShutdown(CloseReason reason)
    {
    	// If an open operation is stuck, this lets us shut down anyway.
    	CompletableFuture<?> captured = this.internalOperationFuture;
    	if (captured != null)
    	{
    		captured.cancel(true);
    	}
    	
    	if (this.partitionReceiver != null)
    	{
    		// Disconnect any processor from the receiver so the processor won't get
    		// any more calls. But a call could be in progress right now. 
    		this.partitionReceiver.setReceiveHandler(null);
    		
            // Close the EH clients. Errors are swallowed, nothing we could do about them anyway.
            cleanUpClients();
    	}
    }
    
    
    private class InternalReceiveHandler extends PartitionReceiveHandler
    {
    	InternalReceiveHandler()
    	{
    		super(EventHubPartitionPump.this.host.getEventProcessorOptions().getMaxBatchSize());
    	}
    	
		@Override
		public void onReceive(Iterable<EventData> events)
		{
        	// This method is called on the thread that the Java EH client uses to run the pump.
        	// There is one pump per EventHubClient. Since each PartitionPump creates a new EventHubClient,
        	// using that thread to call onEvents does no harm. Even if onEvents is slow, the pump will
        	// get control back each time onEvents returns, and be able to receive a new batch of messages
        	// with which to make the next onEvents call. The pump gains nothing by running faster than onEvents.
			
			EventHubPartitionPump.this.onEvents(events);
		}

		@Override
		public void onError(Throwable error)
		{
			if (error == null)
			{
				error = new Throwable("No error info supplied by EventHub client");
			}
			if (error instanceof ReceiverDisconnectedException)
			{
				EventHubPartitionPump.this.host.logWithHostAndPartition(Level.WARNING, EventHubPartitionPump.this.partitionContext,
						"EventHub client disconnected, probably another host took the partition");
			}
			else
			{
				EventHubPartitionPump.this.host.logWithHostAndPartition(Level.SEVERE, EventHubPartitionPump.this.partitionContext, "EventHub client error: " + error.toString());
				if (error instanceof Exception)
				{
					EventHubPartitionPump.this.host.logWithHostAndPartition(Level.SEVERE, EventHubPartitionPump.this.partitionContext, "EventHub client error continued", (Exception)error);
				}
				EventHubPartitionPump.this.onError(error);
			}
			EventHubPartitionPump.this.pumpStatus = PartitionPumpStatus.PP_ERRORED;
		}
    }
}
