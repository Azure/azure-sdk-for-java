// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.fullfidelity;

import com.azure.cosmos.util.Beta;

import java.time.Instant;

/**
 * The metadata of a change feed resource with ChangeFeedMode set to FULL_FIDELITY
 */
@Beta(value = Beta.SinceVersion.V4_28_0)
public class ChangeFeedMetadata {
    private final Instant conflictResolutionTimestamp;
    private final long logSequenceNumber;
    private final ChangeFeedOperationType operationType;
    private final long previousLogSequenceNumber;

    public ChangeFeedMetadata(
        Instant conflictResolutionTimestamp,
        long logSequenceNumber,
        ChangeFeedOperationType operationType,
        long previousLogSequenceNumber
    ) {
        this.conflictResolutionTimestamp = conflictResolutionTimestamp;
        this.logSequenceNumber = logSequenceNumber;
        this.operationType = operationType;
        this.previousLogSequenceNumber = previousLogSequenceNumber;
    }

    /**
     * The conflict resolution timestamp.
     */
    public Instant getConflictResolutionTimestamp(){ return this.conflictResolutionTimestamp; }

    /**
     * The current logical sequence number.
     */
    public long getLogSequenceNumber() { return this.logSequenceNumber; }

    /**
     * The change feed operation type.
     */
    public ChangeFeedOperationType getOperationType() { return this.operationType; }

    /**
     * The previous logical sequence number.
     */
    public long getPreviousLogSequenceNumber() { return this.previousLogSequenceNumber; }

    /**
     * Used to distinguish explicit deletes (e.g. via DeleteItem) from deletes caused by TTL expiration (a collection may define time-to-live policy for documents).
     */
    public boolean timeToLiveExpired() { return this.timeToLiveExpired; }
}
