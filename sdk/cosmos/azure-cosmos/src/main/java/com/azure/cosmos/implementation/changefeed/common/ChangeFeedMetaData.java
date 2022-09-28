// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Change Feed response meta data
 */
public final class ChangeFeedMetaData {
    @JsonProperty("crts")
    private long conflictResolutionTimestamp;
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
    public long getConflictResolutionTimestamp() {
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
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert object to string", e);
        }
    }
}
