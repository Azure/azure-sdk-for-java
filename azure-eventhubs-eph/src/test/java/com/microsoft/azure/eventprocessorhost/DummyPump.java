/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class DummyPump extends Pump
{
	private Set<String> pumps = Collections.synchronizedSet(new HashSet<String>());
	
	public DummyPump(EventProcessorHost host)
	{
		super(host);
	}
	
	Iterable<String> getPumpsList()
	{
		return this.pumps;
	}
	
	void fastCleanup()
	{
		HashSet<String> capturedList = new HashSet<String>(this.pumps); // avoid deadlock!
		for (String p : capturedList)
		{
			Lease l = null;
			try
			{
				l = this.host.getLeaseManager().getLease(p).get();
			} 
			catch (InterruptedException | ExecutionException e)
			{
				continue;
			}
			
			if (this.host.getHostName().compareTo(l.getOwner()) != 0)
			{
				// Another host has stolen this lease.
				try
				{
					TestUtilities.log("Steal detected, host " + this.host.getHostName() + " removing " + p);
					removePump(p, CloseReason.LeaseLost).get();
				}
				catch (InterruptedException | ExecutionException e)
				{
				}
			}
			else
			{
				
			}
		}
	}
	
	//
	// Completely override all functionality.
	//
	
	@Override
    public void addPump(String partitionId, Lease lease) throws Exception
    {
		this.pumps.add(partitionId);
    }
    
	@Override
    public Future<?> removePump(String partitionId, final CloseReason reason)
    {
		return EventProcessorHost.getExecutorService().submit(() -> this.pumps.remove(partitionId));
    }
    
	@Override
    public Iterable<Future<?>> removeAllPumps(CloseReason reason)
    {
		ArrayList<Future<?>> futures = new ArrayList<Future<?>>();
		ArrayList<String> capturedPumps = new ArrayList<String>(this.pumps);
		for (String p : capturedPumps)
		{
			futures.add(removePump(p, reason));
		}
		return futures;
    }
    
	@Override
    public boolean hasPump(String partitionId)
    {
		return this.pumps.contains(partitionId);
    }    
}
