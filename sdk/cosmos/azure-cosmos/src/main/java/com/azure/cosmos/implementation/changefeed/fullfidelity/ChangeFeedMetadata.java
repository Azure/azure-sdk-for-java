// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.fullfidelity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * The metadata of a change feed resource with ChangeFeedMode set to FULL_FIDELITY
 */
public class ChangeFeedMetadata {
    @JsonProperty(value = "crts", access = JsonProperty.Access.WRITE_ONLY)
    private final Instant conflictResolutionTimestamp;
    @JsonProperty(value = "lsn", access = JsonProperty.Access.WRITE_ONLY)
    private final long logSequenceNumber;
    @JsonProperty(value = "operationType", access = JsonProperty.Access.WRITE_ONLY)
    private final ChangeFeedOperationType operationType;
    @JsonProperty(value = "previousImageLSN", access = JsonProperty.Access.WRITE_ONLY)
    private final long previousLogSequenceNumber;
    @JsonProperty(value = "timeToLiveExpired", access = JsonProperty.Access.WRITE_ONLY)
    private final boolean timeToLiveExpired;

    public ChangeFeedMetadata(
        Instant conflictResolutionTimestamp,
        long logSequenceNumber,
        ChangeFeedOperationType operationType,
        long previousLogSequenceNumber,
        boolean timeToLiveExpired
    ) {
        this.conflictResolutionTimestamp = conflictResolutionTimestamp;
        this.logSequenceNumber = logSequenceNumber;
        this.operationType = operationType;
        this.previousLogSequenceNumber = previousLogSequenceNumber;
        this.timeToLiveExpired = timeToLiveExpired;
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
