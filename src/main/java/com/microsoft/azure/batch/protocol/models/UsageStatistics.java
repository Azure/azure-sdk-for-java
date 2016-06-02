/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import org.joda.time.Period;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Statistics related to pool usage information.
 */
public class UsageStatistics {
    /**
     * The start time of the time range covered by the statistics.
     */
    @JsonProperty(required = true)
    private DateTime startTime;

    /**
     * The time at which the statistics were last updated. All statistics are
     * limited to the range between startTime and lastUpdateTime.
     */
    @JsonProperty(required = true)
    private DateTime lastUpdateTime;

    /**
     * The aggregated wall-clock time of the dedicated compute node cores
     * being part of the pool.
     */
    @JsonProperty(required = true)
    private Period dedicatedCoreTime;

    /**
     * Get the startTime value.
     *
     * @return the startTime value
     */
    public DateTime startTime() {
        return this.startTime;
    }

    /**
     * Set the startTime value.
     *
     * @param startTime the startTime value to set
     * @return the UsageStatistics object itself.
     */
    public UsageStatistics withStartTime(DateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Get the lastUpdateTime value.
     *
     * @return the lastUpdateTime value
     */
    public DateTime lastUpdateTime() {
        return this.lastUpdateTime;
    }

    /**
     * Set the lastUpdateTime value.
     *
     * @param lastUpdateTime the lastUpdateTime value to set
     * @return the UsageStatistics object itself.
     */
    public UsageStatistics withLastUpdateTime(DateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    /**
     * Get the dedicatedCoreTime value.
     *
     * @return the dedicatedCoreTime value
     */
    public Period dedicatedCoreTime() {
        return this.dedicatedCoreTime;
    }

    /**
     * Set the dedicatedCoreTime value.
     *
     * @param dedicatedCoreTime the dedicatedCoreTime value to set
     * @return the UsageStatistics object itself.
     */
    public UsageStatistics withDedicatedCoreTime(Period dedicatedCoreTime) {
        this.dedicatedCoreTime = dedicatedCoreTime;
        return this;
    }

}
