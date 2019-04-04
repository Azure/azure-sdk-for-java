// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.time.Instant;

public final class PartitionRuntimeInformation {

    private final String eventHubPath;
    private final String partitionId;
    private final long beginSequenceNumber;
    private final long lastEnqueuedSequenceNumber;
    private final String lastEnqueuedOffset;
    private final Instant lastEnqueuedTimeUtc;
    private final boolean isEmpty;

    public PartitionRuntimeInformation(
            final String eventHubPath,
            final String partitionId,
            final long beginSequenceNumber,
            final long lastEnqueuedSequenceNumber,
            final String lastEnqueuedOffset,
            final Instant lastEnqueuedTimeUtc,
            final boolean isEmpty) {

        this.eventHubPath = eventHubPath;
        this.partitionId = partitionId;
        this.beginSequenceNumber = beginSequenceNumber;
        this.lastEnqueuedSequenceNumber = lastEnqueuedSequenceNumber;
        this.lastEnqueuedOffset = lastEnqueuedOffset;
        this.lastEnqueuedTimeUtc = lastEnqueuedTimeUtc;
        this.isEmpty = isEmpty;
    }

    public String getEventHubPath() {
        return this.eventHubPath;
    }

    public String getPartitionId() {
        return this.partitionId;
    }

    public long getBeginSequenceNumber() {
        return this.beginSequenceNumber;
    }

    public long getLastEnqueuedSequenceNumber() {
        return this.lastEnqueuedSequenceNumber;
    }

    public String getLastEnqueuedOffset() {
        return this.lastEnqueuedOffset;
    }

    public Instant getLastEnqueuedTimeUtc() {
        return this.lastEnqueuedTimeUtc;
    }

    public boolean getIsEmpty() {
        return this.isEmpty;
    }
}
