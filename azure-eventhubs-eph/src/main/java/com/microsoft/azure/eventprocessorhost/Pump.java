/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;


class Pump
{
    protected final EventProcessorHost host; // protected for testability

    private ConcurrentHashMap<String, PartitionPump> pumpStates;
    
    public Pump(EventProcessorHost host)
    {
        this.host = host;

        this.pumpStates = new ConcurrentHashMap<String, PartitionPump>();
    }
    
    public void addPump(String partitionId, Lease lease) throws Exception
    {
    	PartitionPump capturedPump = this.pumpStates.get(partitionId);
    	if (capturedPump != null)
    	{
    		// There already is a pump. Make sure the pump is working and replace the lease.
    		if ((capturedPump.getPumpStatus() == PartitionPumpStatus.PP_ERRORED) || capturedPump.isClosing())
    		{
    			// The existing pump is bad. Remove it (if it exists!) and create a new one.
    			Future<?> removing = removePump(partitionId, CloseReason.Shutdown);
    			if (removing != null)
    			{
    				removing.get();
    			}
    			createNewPump(partitionId, lease);
    		}
    		else
    		{
    			// Pump is working, just replace the lease.
    			this.host.logWithHostAndPartition(Level.FINE, partitionId, "updating lease for pump");
    			capturedPump.setLease(lease);
    		}
    	}
    	else
    	{
    		// No existing pump, create a new one.
    		createNewPump(partitionId, lease);
    	}
    }
    
    private void createNewPump(String partitionId, Lease lease) throws Exception
    {
		PartitionPump newPartitionPump = new EventHubPartitionPump(this.host, this, lease);
		EventProcessorHost.getExecutorService().submit(() -> newPartitionPump.startPump());
        this.pumpStates.put(partitionId, newPartitionPump); // do the put after start, if the start fails then put doesn't happen
		this.host.logWithHostAndPartition(Level.INFO, partitionId, "created new pump");
    }
    
    public Future<?> removePump(String partitionId, final CloseReason reason)
    {
    	Future<?> retval = null;
    	PartitionPump capturedPump = this.pumpStates.get(partitionId);
    	if (capturedPump != null)
    	{
			this.host.logWithHostAndPartition(Level.INFO, partitionId, "closing pump for reason " + reason.toString());
			retval = EventProcessorHost.getExecutorService().submit(() -> capturedPump.shutdown(reason));
    		
    		this.host.logWithHostAndPartition(Level.INFO, partitionId, "removing pump");
    		this.pumpStates.remove(partitionId);
    	}
    	else
    	{
    		// PartitionManager main loop tries to remove pump for every partition that the host does not own, just to be sure.
    		// Not finding a pump for a partition is normal and expected most of the time.
    		this.host.logWithHostAndPartition(Level.FINE, partitionId, "no pump found to remove for partition " + partitionId);
    	}
    	return retval;
    }
    
    void onPumpError(String partitionId)
    {
    	Future<?> removal = removePump(partitionId, CloseReason.Shutdown);
    	if (removal != null)
    	{
			try
			{
				removal.get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				this.host.logWithHostAndPartition(Level.WARNING, partitionId, "error while shutting down failed partition pump", e);
			}
    	}
    }
    
    public Iterable<Future<?>> removeAllPumps(CloseReason reason)
    {
    	ArrayList<Future<?>> futures = new ArrayList<Future<?>>();
    	for (String partitionId : this.pumpStates.keySet())
    	{
    		futures.add(removePump(partitionId, reason));
    	}
    	return futures;
    }
    
    public boolean hasPump(String partitionId)
    {
    	return this.pumpStates.containsKey(partitionId);
    }
}
