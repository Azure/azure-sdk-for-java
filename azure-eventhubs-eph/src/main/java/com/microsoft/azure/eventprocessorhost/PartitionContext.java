/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Level;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.PartitionReceiver;

public class PartitionContext
{
	private final EventProcessorHost host;
    private final String partitionId;
    private final String eventHubPath;
    private final String consumerGroupName;
    
    private Lease lease;
    private String offset = PartitionReceiver.START_OF_STREAM;
    private long sequenceNumber = 0;;
    
    private Object offsetSynchronizer;
    
    PartitionContext(EventProcessorHost host, String partitionId, String eventHubPath, String consumerGroupName)
    {
        this.host = host;
        this.partitionId = partitionId;
        this.eventHubPath = eventHubPath;
        this.consumerGroupName = consumerGroupName;
        
        this.offsetSynchronizer = new Object();
    }

    public String getConsumerGroupName()
    {
        return this.consumerGroupName;
    }

    public String getEventHubPath()
    {
        return this.eventHubPath;
    }
    
    public String getOwner()
    {
    	return this.lease.getOwner();
    }

    Lease getLease()
    {
        return this.lease;
    }

    // Unlike other properties which are immutable after creation, the lease is updated dynamically and needs a setter.
    void setLease(Lease lease)
    {
        this.lease = lease;
    }

    /**
     * Updates the offset/sequenceNumber in the PartitionContext with the values in the received EventData object.
     *  
     * Since offset is a string it cannot be compared easily, but sequenceNumber is checked. The new sequenceNumber must be
     * at least the same as the current value or the entire assignment is aborted. It is assumed that if the new sequenceNumber
     * is equal or greater, the new offset will be as well.
     * 
     * @param event  A received EventData with valid offset and sequenceNumber
     * @throws IllegalArgumentException  If the sequenceNumber in the provided event is less than the current value
     */
    public void setOffsetAndSequenceNumber(EventData event) throws IllegalArgumentException
    {
    	setOffsetAndSequenceNumber(event.getSystemProperties().getOffset(), event.getSystemProperties().getSequenceNumber());
    }
    
    /**
     * Updates the offset/sequenceNumber in the PartitionContext.
     * 
     * These two values are closely tied and must be updated in an atomic fashion, hence the combined setter.
     * Since offset is a string it cannot be compared easily, but sequenceNumber is checked. The new sequenceNumber must be
     * at least the same as the current value or the entire assignment is aborted. It is assumed that if the new sequenceNumber
     * is equal or greater, the new offset will be as well.
     * 
     * @param offset  New offset value
     * @param sequenceNumber  New sequenceNumber value 
     * @throws IllegalArgumentException  If the new sequenceNumber is less than the current value
     */
    public void setOffsetAndSequenceNumber(String offset, long sequenceNumber) throws IllegalArgumentException
    {
    	synchronized (this.offsetSynchronizer)
    	{
    		if (sequenceNumber >= this.sequenceNumber)
    		{
    			this.offset = offset;
    			this.sequenceNumber = sequenceNumber;
    		}
    		else
    		{
    			throw new IllegalArgumentException("new offset " + offset + "//" + sequenceNumber + " less than old " + this.offset + "//" + this.sequenceNumber);
    		}
    	}
    }
    
    public String getPartitionId()
    {
    	return this.partitionId;
    }
    
    // Returns a String (offset) or Instant (timestamp).
    Object getInitialOffset() throws InterruptedException, ExecutionException
    {
    	Object startAt = null;
    	
    	Checkpoint startingCheckpoint = this.host.getCheckpointManager().getCheckpoint(this.partitionId).get();
    	if (startingCheckpoint == null)
    	{
    		// No checkpoint was ever stored. Use the initialOffsetProvider instead.
        	Function<String, Object> initialOffsetProvider = this.host.getEventProcessorOptions().getInitialOffsetProvider();
    		this.host.logWithHostAndPartition(Level.FINE, this.partitionId, "Calling user-provided initial offset provider");
    		startAt = initialOffsetProvider.apply(this.partitionId);
    		if (startAt instanceof String)
    		{
    			this.offset = (String)startAt;
        		this.sequenceNumber = 0; // TODO we use sequenceNumber to check for regression of offset, 0 could be a problem until it gets updated from an event
    	    	this.host.logWithHostAndPartition(Level.FINE, this.partitionId, "Initial offset provided: " + this.offset + "//" + this.sequenceNumber);
    		}
    		else if (startAt instanceof Instant)
    		{
    			// can't set offset/sequenceNumber
    	    	this.host.logWithHostAndPartition(Level.FINE, this.partitionId, "Initial timestamp provided: " + (Instant)startAt);
    		}
    		else
    		{
    			throw new IllegalArgumentException("Unexpected object type returned by user-provided initialOffsetProvider");
    		}
    	}
    	else
    	{
    		// Checkpoint is valid, use it.
	    	this.offset = startingCheckpoint.getOffset();
	    	this.sequenceNumber = startingCheckpoint.getSequenceNumber();
	    	this.host.logWithHostAndPartition(Level.FINE, this.partitionId, "Retrieved starting offset " + this.offset + "//" + this.sequenceNumber);
    	}
    	
    	return startAt;
    }

    /**
     * Writes the current offset and sequenceNumber to the checkpoint store via the checkpoint manager.
     * @throws IllegalArgumentException  If this.sequenceNumber is less than the last checkpointed value  
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public void checkpoint() throws IllegalArgumentException, InterruptedException, ExecutionException
    {
    	// Capture the current offset and sequenceNumber. Synchronize to be sure we get a matched pair
    	// instead of catching an update halfway through. The capturing may not be strictly necessary,
    	// since checkpoint() is called from the user's event processor which also controls the retrieval
    	// of events, and no other thread should be updating this PartitionContext, unless perhaps the
    	// event processor is itself multithreaded... Whether it's required or not, the amount of work
    	// required is trivial, so we might as well do it to be sure.
    	Checkpoint capturedCheckpoint = null;
    	synchronized (this.offsetSynchronizer)
    	{
    		capturedCheckpoint = new Checkpoint(this.partitionId, this.offset, this.sequenceNumber);
    	}
    	persistCheckpoint(capturedCheckpoint);
    }

    /**
     * Stores the offset and sequenceNumber from the provided received EventData instance, then writes those
     * values to the checkpoint store via the checkpoint manager.
     *  
     * @param event  A received EventData with valid offset and sequenceNumber
     * @throws IllegalArgumentException  If the sequenceNumber in the provided event is less than the last checkpointed value  
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public void checkpoint(EventData event) throws IllegalArgumentException, InterruptedException, ExecutionException
    {
    	setOffsetAndSequenceNumber(event.getSystemProperties().getOffset(), event.getSystemProperties().getSequenceNumber());
    	persistCheckpoint(new Checkpoint(this.partitionId, event.getSystemProperties().getOffset(), event.getSystemProperties().getSequenceNumber()));
    }
    
    private void persistCheckpoint(Checkpoint persistThis) throws IllegalArgumentException, InterruptedException, ExecutionException
    {
    	this.host.logWithHostAndPartition(Level.FINE, persistThis.getPartitionId(), "Saving checkpoint: " +
    			persistThis.getOffset() + "//" + persistThis.getSequenceNumber());
		
    	Checkpoint inStoreCheckpoint = this.host.getCheckpointManager().getCheckpoint(persistThis.getPartitionId()).get();
    	if ((inStoreCheckpoint == null) || (persistThis.getSequenceNumber() >= inStoreCheckpoint.getSequenceNumber()))
    	{
        	if (inStoreCheckpoint == null)
        	{
        		inStoreCheckpoint = this.host.getCheckpointManager().createCheckpointIfNotExists(persistThis.getPartitionId()).get();
        	}
	    	inStoreCheckpoint.setOffset(persistThis.getOffset());
	    	inStoreCheckpoint.setSequenceNumber(persistThis.getSequenceNumber());
	        this.host.getCheckpointManager().updateCheckpoint(inStoreCheckpoint).get();
    	}
    	else
    	{
    		String msg = "Ignoring out of date checkpoint with offset " + persistThis.getOffset() + "/sequence number " + persistThis.getSequenceNumber() +
        			" because current persisted checkpoint has higher offset " + inStoreCheckpoint.getOffset() +
        			"/sequence number " + inStoreCheckpoint.getSequenceNumber(); 
    		this.host.logWithHostAndPartition(Level.SEVERE, persistThis.getPartitionId(), msg);
    		throw new IllegalArgumentException(msg);
    	}
    }
}
