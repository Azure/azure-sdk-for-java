/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.PartitionReceiver;

/**
 * Checkpoint class is public so that advanced users can implement an ICheckpointManager. 
 * Unless you are implementing ICheckpointManager you should not have to deal with objects
 * of this class directly.
 * <p>
 * A Checkpoint is essentially just a tuple. It has a fixed partition id, set at creation time
 * and immutable thereafter, and associates that with an offset/sequenceNumber pair which
 * indicates a position within the messages in that partition.
 */
public class Checkpoint
{
	private final String partitionId;
	private String offset = PartitionReceiver.START_OF_STREAM;
	private long sequenceNumber = 0;
	
	/**
	 * Create a checkpoint with offset/sequenceNumber set to the first available message.
	 * 
	 * @param partitionId
	 */
	public Checkpoint(String partitionId)
	{
		this.partitionId = partitionId;
	}
	
	/**
	 * Create a checkpoint with the given offset and sequenceNumber.
	 * 
	 * @param partitionId
	 * @param offset
	 * @param sequenceNumber
	 */
	public Checkpoint(String partitionId, String offset, long sequenceNumber)
	{
		this.partitionId = partitionId;
		this.offset = offset;
		this.sequenceNumber = sequenceNumber;
	}
	
	/**
	 * Create a checkpoint which is a duplicate of the given checkpoint.
	 * 
	 * @param source
	 */
	public Checkpoint(Checkpoint source)
	{
		this.partitionId = source.partitionId;
		this.offset = source.offset;
		this.sequenceNumber = source.sequenceNumber;
	}
	
	/**
	 * Set the offset. Should be paired with setSequenceNumber since the two values are
	 * connected in the Event Hub.
	 * 
	 * @param newOffset the new offset to be persisted.
	 */
	public void setOffset(String newOffset)
	{
		this.offset = newOffset;
	}

	/**
	 * Return the offset.
	 * 
	 * @return the current offset string value.
	 */
	public String getOffset()
	{
		return this.offset;
	}
	
	/**
	 * Set the sequence number. Should be paired with setOffset since the two values are
	 * connected in the Event Hub.
	 * 
	 * @param newSequenceNumber the new sequence number to be persisted.
	 */
	public void setSequenceNumber(long newSequenceNumber)
	{
		this.sequenceNumber = newSequenceNumber;
	}
	
	/**
	 * Get the sequence number.
	 * 
	 * @return the current sequence number.
	 */
	public long getSequenceNumber()
	{
		return this.sequenceNumber;
	}
	
	/**
	 * Get the partition id. There is no corresponding setter because the partition id is immutable.
	 * 
	 * @return get the current partition id that the processing is associated with.
	 */
	public String getPartitionId()
	{
		return this.partitionId;
	}
}
