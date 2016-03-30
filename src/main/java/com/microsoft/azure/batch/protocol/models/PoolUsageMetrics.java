/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Usage metrics for a pool across an aggregation interval.
 */
public class PoolUsageMetrics {
    /**
     * Gets or sets the id of the pool whose metrics are being aggregated.
     */
    @JsonProperty(required = true)
    private String poolId;

    /**
     * Gets or sets the start time of the aggregation interval.
     */
    @JsonProperty(required = true)
    private DateTime startTime;

    /**
     * Gets or sets the end time of the aggregation interval.
     */
    @JsonProperty(required = true)
    private DateTime endTime;

    /**
     * Gets or sets the size of virtual machines in the pool.  All VMs in a
     * pool are the same size.
     */
    @JsonProperty(required = true)
    private String vmSize;

    /**
     * Gets or sets the total core hours used in the pool during this
     * aggregation interval.
     */
    @JsonProperty(required = true)
    private double totalCoreHours;

    /**
     * Gets or sets the cross data center network ingress in GiB to the pool
     * during this interval.
     */
    @JsonProperty(required = true)
    private double dataIngressGiB;

    /**
     * Gets or sets the cross data center network egress in GiB from the pool
     * during this interval.
     */
    @JsonProperty(required = true)
    private double dataEgressGiB;

    /**
     * Get the poolId value.
     *
     * @return the poolId value
     */
    public String getPoolId() {
        return this.poolId;
    }

    /**
     * Set the poolId value.
     *
     * @param poolId the poolId value to set
     */
    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }

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
     * Get the endTime value.
     *
     * @return the endTime value
     */
    public DateTime getEndTime() {
        return this.endTime;
    }

    /**
     * Set the endTime value.
     *
     * @param endTime the endTime value to set
     */
    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Get the vmSize value.
     *
     * @return the vmSize value
     */
    public String getVmSize() {
        return this.vmSize;
    }

    /**
     * Set the vmSize value.
     *
     * @param vmSize the vmSize value to set
     */
    public void setVmSize(String vmSize) {
        this.vmSize = vmSize;
    }

    /**
     * Get the totalCoreHours value.
     *
     * @return the totalCoreHours value
     */
    public double getTotalCoreHours() {
        return this.totalCoreHours;
    }

    /**
     * Set the totalCoreHours value.
     *
     * @param totalCoreHours the totalCoreHours value to set
     */
    public void setTotalCoreHours(double totalCoreHours) {
        this.totalCoreHours = totalCoreHours;
    }

    /**
     * Get the dataIngressGiB value.
     *
     * @return the dataIngressGiB value
     */
    public double getDataIngressGiB() {
        return this.dataIngressGiB;
    }

    /**
     * Set the dataIngressGiB value.
     *
     * @param dataIngressGiB the dataIngressGiB value to set
     */
    public void setDataIngressGiB(double dataIngressGiB) {
        this.dataIngressGiB = dataIngressGiB;
    }

    /**
     * Get the dataEgressGiB value.
     *
     * @return the dataEgressGiB value
     */
    public double getDataEgressGiB() {
        return this.dataEgressGiB;
    }

    /**
     * Set the dataEgressGiB value.
     *
     * @param dataEgressGiB the dataEgressGiB value to set
     */
    public void setDataEgressGiB(double dataEgressGiB) {
        this.dataEgressGiB = dataEgressGiB;
    }

}
