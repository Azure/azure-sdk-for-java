// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.Instant;

/**
 * Change Feed response meta data
 */
@Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class ChangeFeedMetaData {
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
    @Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Instant getConflictResolutionTimestamp() {
        return conflictResolutionTimestamp;
    }

    /**
     * Gets the current logical sequence number
     *
     * @return current logical sequence number
     */
    @Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public long getLogSequenceNumber() {
        return logSequenceNumber;
    }

    /**
     * Gets the Change Feed operation type
     *
     * @return change Feed operation type
     */
    @Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ChangeFeedOperationType getOperationType() {
        return operationType;
    }

    /**
     * Gets the previous logical sequence number
     *
     * @return previous logical sequence number
     */
    @Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public long getPreviousLogSequenceNumber() {
        return previousLogSequenceNumber;
    }

    /**
     * Used to distinguish explicit deletes (e.g. via deleteItem() API) from deletes caused by TTL expiration
     * (a collection may define time-to-live policy for documents).
     *
     * @return true if ttlExpiration caused the delete.
     */
    @Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public boolean isTimeToLiveExpired() {
        return timeToLiveExpired;
    }

    @Override
    public String toString() {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert object to string", e);
        }
    }
}
