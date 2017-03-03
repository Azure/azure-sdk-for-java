/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.time.Instant;

/**
 * Represents the temporal receiver runtime information for a {@link PartitionReceiver}.
 * Current received {@link EventData} and {@link ReceiverRuntimeInformation} can be used to find approximate value of pending events (which are not processed yet).
 */
public final class ReceiverRuntimeInformation {
    
    private final String partitionId;
    
    private long lastSequenceNumber;
    private Instant lastEnqueuedTime;
    private String lastEnqueuedOffset;
    private Instant retrievalTime;
    
    public ReceiverRuntimeInformation(final String partitionId) {
        
        this.partitionId = partitionId;
    }
    
    /**
     * Get PartitionId of the {@link PartitionReceiver} for which the {@link ReceiverRuntimeInformation} is returned.
     * @return Partition Identifier
     */
    public String getPartitionId() {
        
        return this.partitionId;
    }
    
    /**
     * Get sequence number of the {@link EventData}, that is written at the end of the Partition Stream.
     * @return last sequence number
     */
    public long getLastSequenceNumber() {
        
        return this.lastSequenceNumber;
    }
    
    /**
     * Get enqueued time of the {@link EventData}, that is written at the end of the Partition Stream.
     * @return last enqueued time
     */
    public Instant getLastEnqueuedTime() {
        
        return this.lastEnqueuedTime;
    }
    
    /**
     * Get offset of the {@link Eventdata}, that is written at the end of the Partition Stream.
     * @return last enqueued offset
     */
    public String getLastEnqueuedOffset() {
        
        return this.lastEnqueuedOffset;
    }
    
    /**
     * Get the timestamp at which this {@link ReceiverRuntimeInformation} was constructed.
     * @return retrieval time
     */
    public Instant getRetrievalTime() {
        
        return this.retrievalTime;
    }
    
    void setRuntimeInformation(final long sequenceNumber, final Instant enqueuedTime, final String offset) {
        
        this.lastSequenceNumber = sequenceNumber;
        this.lastEnqueuedTime = enqueuedTime;
        this.lastEnqueuedOffset = offset;
        
        this.retrievalTime = Instant.now();
    }
}
