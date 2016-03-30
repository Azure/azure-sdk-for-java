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
     * Gets or sets the start time of the time range covered by the statistics.
     */
    @JsonProperty(required = true)
    private DateTime startTime;

    /**
     * Gets or sets the time at which the statistics were last updated. All
     * statistics are limited to the range between startTime and
     * lastUpdateTime.
     */
    @JsonProperty(required = true)
    private DateTime lastUpdateTime;

    /**
     * Gets or sets the aggregated wall-clock time of the dedicated compute
     * node cores being part of the pool.
     */
    @JsonProperty(required = true)
    private Period dedicatedCoreTime;

    /**
     * Get the startTime value.
     *
     * @return the startTime value
     */
    public DateTime getStartTime() {
        return this.startTime;
    }

    /**
     * Set the startTime value.
     *
     * @param startTime the startTime value to set
     */
    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Get the lastUpdateTime value.
     *
     * @return the lastUpdateTime value
     */
    public DateTime getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    /**
     * Set the lastUpdateTime value.
     *
     * @param lastUpdateTime the lastUpdateTime value to set
     */
    public void setLastUpdateTime(DateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    /**
     * Get the dedicatedCoreTime value.
     *
     * @return the dedicatedCoreTime value
     */
    public Period getDedicatedCoreTime() {
        return this.dedicatedCoreTime;
    }

    /**
     * Set the dedicatedCoreTime value.
     *
     * @param dedicatedCoreTime the dedicatedCoreTime value to set
     */
    public void setDedicatedCoreTime(Period dedicatedCoreTime) {
        this.dedicatedCoreTime = dedicatedCoreTime;
    }

}
