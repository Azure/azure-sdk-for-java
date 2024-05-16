// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.impl.ClientConstants;

/**
 * Checkpoint class is public so that advanced users can implement an ICheckpointManager.
 * Unless you are implementing ICheckpointManager you should not have to deal with objects
 * of this class directly.
 * <p>
 * A Checkpoint is essentially just a tuple. It has a fixed partition id, set at creation time
 * and immutable thereafter, and associates that with an offset/sequenceNumber pair which
 * indicates a position within the events in that partition.
 */
public class Checkpoint {
    private final String partitionId;
    private String offset = ClientConstants.START_OF_STREAM;
    private long sequenceNumber = 0;

    /**
     * Create a checkpoint with offset/sequenceNumber set to the start of the stream.
     *
     * @param partitionId Associated partition.
     */
    public Checkpoint(String partitionId) {
        this.partitionId = partitionId;
    }

    /**
     * Create a checkpoint with the given offset and sequenceNumber. It is important that the
     * offset and sequence number refer to the same event in the stream. The safest thing
     * to do is get both values from the system properties of one EventData instance.
     *
     * @param partitionId    Associated partition.
     * @param offset         Offset in the stream.
     * @param sequenceNumber Sequence number in the stream.
     */
    public Checkpoint(String partitionId, String offset, long sequenceNumber) {
        this.partitionId = partitionId;
        this.offset = offset;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Create a checkpoint which is a duplicate of the given checkpoint.
     *
     * @param source Existing checkpoint to clone.
     */
    public Checkpoint(Checkpoint source) {
        this.partitionId = source.partitionId;
        this.offset = source.offset;
        this.sequenceNumber = source.sequenceNumber;
    }

    /**
     * Return the offset.
     *
     * @return the current offset value.
     */
    public String getOffset() {
        return this.offset;
    }

    /**
     * Set the offset. Remember to also set the sequence number!
     *
     * @param newOffset the new value for offset in the stream.
     */
    public void setOffset(String newOffset) {
        this.offset = newOffset;
    }

    /**
     * Get the sequence number.
     *
     * @return the current sequence number.
     */
    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    /**
     * Set the sequence number. Remember to also set the offset!
     *
     * @param newSequenceNumber the new value for sequence number.
     */
    public void setSequenceNumber(long newSequenceNumber) {
        this.sequenceNumber = newSequenceNumber;
    }

    /**
     * Get the partition id. There is no corresponding setter because the partition id is immutable.
     *
     * @return the associated partition id.
     */
    public String getPartitionId() {
        return this.partitionId;
    }
}
