// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.util.Beta;

import java.time.Instant;

/**
 * Specifies the {@link ChangeFeedProcessor} state for a particular lease/worker.
 */
@Beta(Beta.SinceVersion.V4_2_0)
public class ChangeFeedProcessorState {
    private String id;
    private String leaseToken;
    private String hostName;
    private Instant lastUpdatedTime;
    private String continuationToken;
    private Instant continuationTokenTimestamp;
    private int estimatedLag;

    /**
     * Gets the ID of the lease item representing the persistent state of a change feed processor worker.
     *
     * @return the ID of the lease item.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the ID of the lease item representing the persistent state of a chenge feed processor worker.
     *
     * @param id a unique string.
     * @return the current ChangeFeedProcessorState instance.
     */
    public ChangeFeedProcessorState setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the token of the lease item representing the persistent state of a change feed processor worker.
     * <p>
     * A lease token is a unique identifier representing a specific scope that a CFP worker will operate on.
     *
     * @return a string token uniquely representing the scope for one worker unit.
     */
    public String getLeaseToken() {
        return this.leaseToken;
    }

    /**
     * Sets the token of the lease item representing the persistent state of a change feed processor worker.
     * <p>
     * A lease token is a unique identifier representing a specific scope that a CFP worker will operate on.
     *
     * @param leaseToken a unique string representing a specific scope that a CFP worker will operate on.
     * @return the current ChangeFeedProcessorState instance.
     */
    public ChangeFeedProcessorState setLeaseToken(String leaseToken) {
        this.leaseToken = leaseToken;
        return this;
    }

    /**
     * Gets the name of the host which operates on the lease item.
     * <p>
     * When using multiple CFP instances distributing the work for a given feed container, each host must have a unique name.
     *
     * @return the host name that has ownership of this lease item or null if no host is currently operating on this lease.
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * Sets the name of the host which operates on the lease item.
     * <p>
     * When using multiple CFP instances distributing the work for a given feed container, each host must have a unique name.
     *
     * @param hostName the host name that has ownership of this lease item.
     * @return the current ChangeFeedProcessorState instance.
     */
    public ChangeFeedProcessorState setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Gets the time when the lease item was last updated.
     *
     * @return time when the lease item was last updated.
     */
    public Instant getLastUpdatedTime() {
        return this.lastUpdatedTime;
    }

    /**
     * Sets time when the lease item was last updated.
     *
     * @param lastUpdatedTime a unique string.
     * @return the current ChangeFeedProcessorState instance.
     */
    public ChangeFeedProcessorState setLastUpdatedTime(Instant lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
        return this;
    }

    /**
     * Gets a marker representing the last item that was processed.
     *
     * @return the marker representing the last item that was processed.
     */
    public String getContinuationToken() {
        return this.continuationToken;
    }

    /**
     * Sets a marker representing the last item that was processed..
     *
     * @param continuationToken the marker representing the last item that was processed.
     * @return the current ChangeFeedProcessorState instance.
     */
    public ChangeFeedProcessorState setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
        return this;
    }

    /**
     * Gets the system time for the last item that was processed.
     *
     * @return the system time for the last item that was processed.
     */
    public Instant getContinuationTokenTimestamp() {
        return this.continuationTokenTimestamp;
    }

    /**
     * Sets the system time for the last item that was processed.
     *
     * @param continuationTokenTimestamp the system time for the last item that was processed.
     * @return the current ChangeFeedProcessorState instance.
     */
    public ChangeFeedProcessorState setContinuationTokenTimestamp(Instant continuationTokenTimestamp) {
        this.continuationTokenTimestamp = continuationTokenTimestamp;
        return this;
    }

    /**
     * Gets an approximation of the difference between the last processed item in the feed container and the
     *   latest change recorded.
     *
     * @return the estimated lag.
     */
    public int getEstimatedLag() {
        return this.estimatedLag;
    }

    /**
     * Sets the estimated lag.
     *
     * @param estimatedLag the estimated lag.
     * @return the current ChangeFeedProcessorState instance.
     */
    public ChangeFeedProcessorState setEstimatedLag(int estimatedLag) {
        this.estimatedLag = estimatedLag;
        return this;
    }
}
