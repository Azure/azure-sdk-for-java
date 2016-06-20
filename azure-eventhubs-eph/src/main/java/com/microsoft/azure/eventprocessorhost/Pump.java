/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
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
    			// The existing pump is bad. Remove it and create a new one.
    			removePump(partitionId, CloseReason.Shutdown).get();
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
		PartitionPump newPartitionPump = new EventHubPartitionPump(this.host, lease);
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
    		if (!capturedPump.isClosing())
    		{
    			retval = EventProcessorHost.getExecutorService().submit(() -> capturedPump.shutdown(reason));
    		}
    		// else, pump is already closing/closed, don't need to try to shut it down again
    		
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
