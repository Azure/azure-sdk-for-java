/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.ReceiverRuntimeInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * PartitionContext is used to provide partition-related information to the methods of IEventProcessor,
 * particularly onEvents where the user's event-processing logic lives. It also allows the user to
 * persist checkpoints for the partition, which determine where event processing will begin if the
 * event processor for that partition must be restarted, such as if ownership of the partition moves
 * from one event processor host instance to another.
 */
public class PartitionContext
{
    private final HostContext hostContext;
    private final String partitionId;
    
    private Lease lease;
    private String offset = null;
    private long sequenceNumber = 0;
    private ReceiverRuntimeInformation runtimeInformation;

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(PartitionContext.class);
    
    PartitionContext(HostContext hostContext, String partitionId)
    {
        this.hostContext = hostContext;
        this.partitionId = partitionId;

        this.runtimeInformation = new ReceiverRuntimeInformation(partitionId);
    }

    /***
     * Get the name of the consumer group that is being received from. 
     * 
     * @return consumer group name
     */
    public String getConsumerGroupName()
    {
        return this.hostContext.getConsumerGroupName();
    }

    /***
     * Get the path of the event hub that is being received from.
     * 
     * @return event hub path
     */
    public String getEventHubPath()
    {
        return this.hostContext.getEventHubPath();
    }

    /***
     * Get the name of the event processor host instance.
     * 
     * @return event processor host instance name
     */
    public String getOwner()
    {
    	return this.lease.getOwner();
    }
    
    /***
     * If receiver runtime metrics have been enabled in EventProcessorHost, this method
     * gets the metrics as they come in.  
     * 
     * @return See ReceiverRuntimeInformation.
     */
    public ReceiverRuntimeInformation getRuntimeInformation()
    {
        return this.runtimeInformation;
    }
    
    void setRuntimeInformation(ReceiverRuntimeInformation value)
    {
        this.runtimeInformation = value;
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

    void setOffsetAndSequenceNumber(EventData event)
    {
		if (event.getSystemProperties().getSequenceNumber() >= this.sequenceNumber)
		{
			this.offset = event.getSystemProperties().getOffset();
			this.sequenceNumber = event.getSystemProperties().getSequenceNumber();
		}
		else
		{
			TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionId,
                    "setOffsetAndSequenceNumber(" + event.getSystemProperties().getOffset() + "//" +
					event.getSystemProperties().getSequenceNumber() + ") would move backwards, ignoring"));
		}
    }
    
    /***
     * Get the id of the partition being received from.
     * 
     * @return partition id
     */
    public String getPartitionId()
    {
    	return this.partitionId;
    }
    
    // Returns a String (offset) or Instant (timestamp).
    CompletableFuture<EventPosition> getInitialOffset()
    {
    	return this.hostContext.getCheckpointManager().getCheckpoint(this.partitionId)
    	.thenApply((startingCheckpoint) ->
    	{
    		return checkpointToOffset(startingCheckpoint);
    	});
    }
    
    EventPosition checkpointToOffset(Checkpoint startingCheckpoint)
    {
    	EventPosition startAt = null;
    	if (startingCheckpoint == null)
    	{
    		// No checkpoint was ever stored. Use the initialOffsetProvider instead.
        	Function<String, EventPosition> initialPositionProvider = this.hostContext.getEventProcessorOptions().getInitialPositionProvider();
    		TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(this.partitionId, "Calling user-provided initial position provider"));
    		startAt = initialPositionProvider.apply(this.partitionId);
    		// Leave this.offset as null. The initialPositionProvider cannot provide enough information to write a valid checkpoint:
    		// at most if will give one of offset or sequence number, and if it is a starting time then it doesn't have either.
	    	TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionId, "Initial position provided: " + startAt));
    	}
    	else
    	{
    		// Checkpoint is valid, use it.
	    	this.offset = startingCheckpoint.getOffset();
	    	startAt = EventPosition.fromOffset(this.offset);
	    	this.sequenceNumber = startingCheckpoint.getSequenceNumber();
	    	TRACE_LOGGER.info(this.hostContext.withHostAndPartition(this.partitionId, "Retrieved starting offset " + this.offset + "//" + this.sequenceNumber));
    	}
    	
    	return startAt;
    }

    /**
     * Writes the position of the last event in the current batch to the checkpoint store via the checkpoint manager.
     * 
     * It is important to check the result in order to detect failures.
     * 
     * If receiving started from a user-provided EventPosition and no events have been received yet,
     * then this will fail. (This scenario is possible when invoke-after-receive-timeout has been set
     * in EventProcessorOptions.)
     * 
     * @return CompletableFuture {@literal ->} null when the checkpoint has been persisted successfully, completes exceptionally on error.
     */
    public CompletableFuture<Void> checkpoint()
    {
    	CompletableFuture<Void> result = null;
    	if (this.offset == null)
    	{
    		result = new CompletableFuture<Void>();
    		result.completeExceptionally(new RuntimeException("Cannot checkpoint until at least one event has been received on this partition"));
    	}
    	else
    	{
	    	Checkpoint capturedCheckpoint = new Checkpoint(this.partitionId, this.offset, this.sequenceNumber);
	    	result = persistCheckpoint(capturedCheckpoint);
    	}
    	return result;
    }

    /**
     * Writes the position of the provided EventData instance to the checkpoint store via the checkpoint manager.
     * 
     * It is important to check the result in order to detect failures.
     *  
     * @param event  A received EventData
     * @return CompletableFuture {@literal ->} null when the checkpoint has been persisted successfully, completes exceptionally on error.
     */
    public CompletableFuture<Void> checkpoint(EventData event)
    {
    	return persistCheckpoint(new Checkpoint(this.partitionId, event.getSystemProperties().getOffset(), event.getSystemProperties().getSequenceNumber()));
    }
    
    private CompletableFuture<Void> persistCheckpoint(Checkpoint persistThis)
    {
    	TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(persistThis.getPartitionId(),
                "Saving checkpoint: " + persistThis.getOffset() + "//" + persistThis.getSequenceNumber()));
		
        return this.hostContext.getCheckpointManager().updateCheckpoint(this.lease, persistThis);
    }
}
