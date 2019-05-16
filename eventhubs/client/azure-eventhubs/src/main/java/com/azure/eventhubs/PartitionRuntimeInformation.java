// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import java.time.Instant;

/**
 * Contains runtime information about an Event Hub partition.
 */
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

    public String eventHubPath() {
        return this.eventHubPath;
    }

    public String partitionId() {
        return this.partitionId;
    }

    public long beginSequenceNumber() {
        return this.beginSequenceNumber;
    }

    public long lastEnqueuedSequenceNumber() {
        return this.lastEnqueuedSequenceNumber;
    }

    public String lastEnqueuedOffset() {
        return this.lastEnqueuedOffset;
    }

    public Instant lastEnqueuedTimeUtc() {
        return this.lastEnqueuedTimeUtc;
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }
}
