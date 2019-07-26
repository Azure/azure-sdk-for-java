// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import java.time.Duration;

/**
 * Specifies the frequency of lease event. The event will trigger when either of conditions is satisfied.
 */
public class CheckpointFrequency {
    private boolean explicitCheckpoint;
    private int processedDocumentCount;
    private Duration timeInterval;

    public CheckpointFrequency() {
        this.explicitCheckpoint = false;
        // DEFAULT to always checkpoint after processing each feed batch.
        processedDocumentCount = 0;
        timeInterval = null;
    }

    /**
     * Gets a value indicating whether explicit check-pointing is enabled.
     * <p>
     * By default false. Setting to true means changefeed host will never checkpoint and client code needs to explicitly
     *   checkpoint via {@link PartitionCheckpointer}
     *
     * @return a value indicating whether explicit check-pointing is enabled.
     */
    public boolean isExplicitCheckpoint() {
        return explicitCheckpoint;
    }

    /**
     * Gets the value that specifies to checkpoint every specified number of docs.
     *
     * @return the value that specifies to checkpoint every specified number of docs.
     */
    public int getProcessedDocumentCount() {
        return this.processedDocumentCount;
    }

    /**
     * Gets the value that specifies to checkpoint every specified time interval.
     *
     * @return the value that specifies to checkpoint every specified time interval.
     */
    public Duration getTimeInterval() {
        return this.timeInterval;
    }

    /**
     * Sets a value indicating explicit check-pointing is enabled.
     *
     * @return current {@link CheckpointFrequency}.
     */
    public CheckpointFrequency withExplicitCheckpoint() {
        this.explicitCheckpoint = true;
        return this;
    }

    /**
     * Sets a value indicating explicit checkpointing is disabled.
     *
     * @return current {@link CheckpointFrequency}.
     */
    public CheckpointFrequency withoutExplicitCheckpoint() {
        this.explicitCheckpoint = false;
        return this;
    }

    /**
     * Sets the value that specifies to checkpoint every specified number of docs.
     *
     * @param processedDocumentCount the value that specifies to checkpoint every specified number of docs.
     * @return current {@link CheckpointFrequency}.
     */
    public CheckpointFrequency withProcessedDocumentCount(int processedDocumentCount) {
        this.processedDocumentCount = processedDocumentCount;
        return this;
    }

    /**
     * Sets the value that specifies to checkpoint every specified time interval.
     *
     * @param timeInterval the value that specifies to checkpoint every specified time interval.
     * @return current {@link CheckpointFrequency}.
     */
    public CheckpointFrequency withTimeInterval(Duration timeInterval) {
        this.timeInterval = timeInterval;
        return this;
    }
}
