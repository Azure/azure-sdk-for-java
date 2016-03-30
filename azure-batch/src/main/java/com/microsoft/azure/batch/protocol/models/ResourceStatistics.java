/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Statistics related to resource consumption by compute nodes in a pool.
 */
public class ResourceStatistics {
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
     * Gets or sets the average CPU usage across all nodes in the pool
     * (percentage per node).
     */
    @JsonProperty(required = true)
    private double avgCPUPercentage;

    /**
     * Gets or sets the average memory usage in GiB across all nodes in the
     * pool.
     */
    @JsonProperty(required = true)
    private double avgMemoryGiB;

    /**
     * Gets or sets the peak memory usage in GiB across all nodes in the pool.
     */
    @JsonProperty(required = true)
    private double peakMemoryGiB;

    /**
     * Gets or sets the average used disk space in GiB across all nodes in the
     * pool.
     */
    @JsonProperty(required = true)
    private double avgDiskGiB;

    /**
     * Gets or sets the peak used disk space in GiB across all nodes in the
     * pool.
     */
    @JsonProperty(required = true)
    private double peakDiskGiB;

    /**
     * Gets or sets the total number of disk read operations across all nodes
     * in the pool.
     */
    @JsonProperty(required = true)
    private long diskReadIOps;

    /**
     * Gets or sets the total number of disk write operations across all nodes
     * in the pool.
     */
    @JsonProperty(required = true)
    private long diskWriteIOps;

    /**
     * Gets or sets the total amount of data in GiB of disk reads across all
     * nodes in the pool.
     */
    @JsonProperty(required = true)
    private double diskReadGiB;

    /**
     * Gets or sets the total amount of data in GiB of disk writes across all
     * nodes in the pool.
     */
    @JsonProperty(required = true)
    private double diskWriteGiB;

    /**
     * Gets or sets the total amount of data in GiB of network reads across
     * all nodes in the pool.
     */
    @JsonProperty(required = true)
    private double networkReadGiB;

    /**
     * Gets or sets the total amount of data in GiB of network writes across
     * all nodes in the pool.
     */
    @JsonProperty(required = true)
    private double networkWriteGiB;

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
     * Get the avgCPUPercentage value.
     *
     * @return the avgCPUPercentage value
     */
    public double getAvgCPUPercentage() {
        return this.avgCPUPercentage;
    }

    /**
     * Set the avgCPUPercentage value.
     *
     * @param avgCPUPercentage the avgCPUPercentage value to set
     */
    public void setAvgCPUPercentage(double avgCPUPercentage) {
        this.avgCPUPercentage = avgCPUPercentage;
    }

    /**
     * Get the avgMemoryGiB value.
     *
     * @return the avgMemoryGiB value
     */
    public double getAvgMemoryGiB() {
        return this.avgMemoryGiB;
    }

    /**
     * Set the avgMemoryGiB value.
     *
     * @param avgMemoryGiB the avgMemoryGiB value to set
     */
    public void setAvgMemoryGiB(double avgMemoryGiB) {
        this.avgMemoryGiB = avgMemoryGiB;
    }

    /**
     * Get the peakMemoryGiB value.
     *
     * @return the peakMemoryGiB value
     */
    public double getPeakMemoryGiB() {
        return this.peakMemoryGiB;
    }

    /**
     * Set the peakMemoryGiB value.
     *
     * @param peakMemoryGiB the peakMemoryGiB value to set
     */
    public void setPeakMemoryGiB(double peakMemoryGiB) {
        this.peakMemoryGiB = peakMemoryGiB;
    }

    /**
     * Get the avgDiskGiB value.
     *
     * @return the avgDiskGiB value
     */
    public double getAvgDiskGiB() {
        return this.avgDiskGiB;
    }

    /**
     * Set the avgDiskGiB value.
     *
     * @param avgDiskGiB the avgDiskGiB value to set
     */
    public void setAvgDiskGiB(double avgDiskGiB) {
        this.avgDiskGiB = avgDiskGiB;
    }

    /**
     * Get the peakDiskGiB value.
     *
     * @return the peakDiskGiB value
     */
    public double getPeakDiskGiB() {
        return this.peakDiskGiB;
    }

    /**
     * Set the peakDiskGiB value.
     *
     * @param peakDiskGiB the peakDiskGiB value to set
     */
    public void setPeakDiskGiB(double peakDiskGiB) {
        this.peakDiskGiB = peakDiskGiB;
    }

    /**
     * Get the diskReadIOps value.
     *
     * @return the diskReadIOps value
     */
    public long getDiskReadIOps() {
        return this.diskReadIOps;
    }

    /**
     * Set the diskReadIOps value.
     *
     * @param diskReadIOps the diskReadIOps value to set
     */
    public void setDiskReadIOps(long diskReadIOps) {
        this.diskReadIOps = diskReadIOps;
    }

    /**
     * Get the diskWriteIOps value.
     *
     * @return the diskWriteIOps value
     */
    public long getDiskWriteIOps() {
        return this.diskWriteIOps;
    }

    /**
     * Set the diskWriteIOps value.
     *
     * @param diskWriteIOps the diskWriteIOps value to set
     */
    public void setDiskWriteIOps(long diskWriteIOps) {
        this.diskWriteIOps = diskWriteIOps;
    }

    /**
     * Get the diskReadGiB value.
     *
     * @return the diskReadGiB value
     */
    public double getDiskReadGiB() {
        return this.diskReadGiB;
    }

    /**
     * Set the diskReadGiB value.
     *
     * @param diskReadGiB the diskReadGiB value to set
     */
    public void setDiskReadGiB(double diskReadGiB) {
        this.diskReadGiB = diskReadGiB;
    }

    /**
     * Get the diskWriteGiB value.
     *
     * @return the diskWriteGiB value
     */
    public double getDiskWriteGiB() {
        return this.diskWriteGiB;
    }

    /**
     * Set the diskWriteGiB value.
     *
     * @param diskWriteGiB the diskWriteGiB value to set
     */
    public void setDiskWriteGiB(double diskWriteGiB) {
        this.diskWriteGiB = diskWriteGiB;
    }

    /**
     * Get the networkReadGiB value.
     *
     * @return the networkReadGiB value
     */
    public double getNetworkReadGiB() {
        return this.networkReadGiB;
    }

    /**
     * Set the networkReadGiB value.
     *
     * @param networkReadGiB the networkReadGiB value to set
     */
    public void setNetworkReadGiB(double networkReadGiB) {
        this.networkReadGiB = networkReadGiB;
    }

    /**
     * Get the networkWriteGiB value.
     *
     * @return the networkWriteGiB value
     */
    public double getNetworkWriteGiB() {
        return this.networkWriteGiB;
    }

    /**
     * Set the networkWriteGiB value.
     *
     * @param networkWriteGiB the networkWriteGiB value to set
     */
    public void setNetworkWriteGiB(double networkWriteGiB) {
        this.networkWriteGiB = networkWriteGiB;
    }

}
