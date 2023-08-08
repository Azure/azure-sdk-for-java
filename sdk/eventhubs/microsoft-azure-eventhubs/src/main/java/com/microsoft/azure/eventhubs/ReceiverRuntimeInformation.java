// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.time.Instant;

/**
 * Represents the temporal end of stream information of an EventHubs Partition.
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
     * The Event Hubs partition id to which this information belongs to
     *
     * @return the partition identifier
     */
    public String getPartitionId() {

        return this.partitionId;
    }

    /**
     * The last enqueued {@link EventData}'s sequence number on this EventHubs Partition
     *
     * @return last enqueued sequence number
     */
    public long getLastEnqueuedSequenceNumber() {

        return this.lastSequenceNumber;
    }

    /**
     * The last enqueued {@link EventData}'s enqueue time stamp on this EventHubs Partition
     *
     * @return last enqueued time
     */
    public Instant getLastEnqueuedTime() {

        return this.lastEnqueuedTime;
    }

    /**
     * The last enqueued {@link EventData}'s offset on this EventHubs Partition
     *
     * @return offset
     */
    public String getLastEnqueuedOffset() {

        return this.lastEnqueuedOffset;
    }

    /**
     * The value indicating when this information was retrieved from the Event Hubs service
     *
     * @return retrieval time
     */
    public Instant getRetrievalTime() {

        return this.retrievalTime;
    }

    public void setRuntimeInformation(final long sequenceNumber, final Instant enqueuedTime, final String offset) {

        this.lastSequenceNumber = sequenceNumber;
        this.lastEnqueuedTime = enqueuedTime;
        this.lastEnqueuedOffset = offset;

        this.retrievalTime = Instant.now();
    }
}
