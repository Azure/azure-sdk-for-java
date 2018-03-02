/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.eventhubs.EventData;


class DummyPump extends Pump
{
	public DummyPump(HostContext hostContext)
	{
		super(hostContext);
	}
	
	Iterable<String> getPumpsList()
	{
		return this.pumpStates.keySet();
	}

	@Override
	protected PartitionPump createNewPump(Lease lease)
	{
		return new DummyPartitionPump(this.hostContext, lease); 
	}
	
	@Override
	protected void removingPumpTestHook(String partitionId, Throwable e)
    {
		TestUtilities.log("Steal detected, host " + this.hostContext.getHostName() + " removing " + partitionId);
    }
	
	
	private class DummyPartitionPump extends PartitionPump implements Callable<Void>
	{
		CompletableFuture<Void> blah = null;
		
		DummyPartitionPump(HostContext hostContext, Lease lease)
		{
			super(hostContext, lease);
		}

		@Override
	    CompletableFuture<Void> startPump()
	    {
			super.setupPartitionContext();
			this.blah = new CompletableFuture<Void>();
			((InMemoryLeaseManager)this.hostContext.getLeaseManager()).notifyOnSteal(this.hostContext.getHostName(), this.lease.getPartitionId(), this);
			super.scheduleLeaseRenewer();
			return this.blah;
	    }
		
		@Override
	    protected void internalShutdown(CloseReason reason, Throwable e)
	    {
			super.cancelPendingOperations();
	    	if (e != null)
	    	{
	    		this.blah.completeExceptionally(e);
	    	}
	    	else
	    	{
	    		this.blah.complete(null);
	    	}
	    }

	    @Override
	    CompletableFuture<Void> shutdown(CloseReason reason)
	    {
	    	internalShutdown(reason, null);
	    	return this.blah;
	    }
	    
		@Override
		public void onReceive(Iterable<EventData> events)
		{
		}

		@Override
	    public void onError(Throwable error)
	    {
	    }

		@Override
		public Void call()
		{
			if (this.blah != null)
			{
				this.blah.completeExceptionally(new LeaseLostException(this.lease, "lease stolen"));
			}
			return null;
		}
	}
}
