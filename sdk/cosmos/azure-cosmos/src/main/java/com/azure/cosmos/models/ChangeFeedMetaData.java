// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Change Feed response meta data
 */
@Beta(value = Beta.SinceVersion.V4_34_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class ChangeFeedMetaData {
    @JsonProperty("crts")
    private Instant conflictResolutionTimestamp;
    @JsonProperty("lsn")
    private long logSequenceNumber;
    @JsonProperty("operationType")
    private ChangeFeedOperationType operationType;
    @JsonProperty("previousImageLSN")
    private long previousLogSequenceNumber;
    @JsonProperty("timeToLiveExpired")
    private boolean timeToLiveExpired;

    /**
     * Gets the conflict resolution timestamp
     *
     * @return conflict resolution timestamp
     */
    public Instant getConflictResolutionTimestamp() {
        return conflictResolutionTimestamp;
    }

    /**
     * Gets the current logical sequence number
     *
     * @return current logical sequence number
     */
    public long getLogSequenceNumber() {
        return logSequenceNumber;
    }

    /**
     * Gets the Change Feed operation type
     *
     * @return change Feed operation type
     */
    public ChangeFeedOperationType getOperationType() {
        return operationType;
    }

    /**
     * Gets the previous logical sequence number
     *
     * @return previous logical sequence number
     */
    public long getPreviousLogSequenceNumber() {
        return previousLogSequenceNumber;
    }

    /**
     * Used to distinguish explicit deletes (e.g. via deleteItem() API) from deletes caused by TTL expiration
     * (a collection may define time-to-live policy for documents).
     *
     * @return true if ttlExpiration caused the delete.
     */
    public boolean isTimeToLiveExpired() {
        return timeToLiveExpired;
    }

    @Override
    public String toString() {
        return "ChangeFeedMetaData{" +
            "conflictResolutionTimestamp=" + conflictResolutionTimestamp +
            ", logSequenceNumber=" + logSequenceNumber +
            ", operationType=" + operationType +
            ", previousLogSequenceNumber=" + previousLogSequenceNumber +
            ", timeToLiveExpired=" + timeToLiveExpired +
            '}';
    }
}
