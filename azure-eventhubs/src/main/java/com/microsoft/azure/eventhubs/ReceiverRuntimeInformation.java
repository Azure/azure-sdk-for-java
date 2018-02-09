/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.time.Instant;

public final class ReceiverRuntimeInformation {

    private final String partitionId;

    private long lastSequenceNumber;
    private Instant lastEnqueuedTime;
    private String lastEnqueuedOffset;
    private Instant retrievalTime;

    public ReceiverRuntimeInformation(final String partitionId) {

        this.partitionId = partitionId;
    }

    public String getPartitionId() {

        return this.partitionId;
    }

    public long getLastSequenceNumber() {

        return this.lastSequenceNumber;
    }

    public Instant getLastEnqueuedTime() {

        return this.lastEnqueuedTime;
    }

    public String getLastEnqueuedOffset() {

        return this.lastEnqueuedOffset;
    }

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
