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
     * The id of the pool whose metrics are being aggregated.
     */
    @JsonProperty(required = true)
    private String poolId;

    /**
     * The start time of the aggregation interval.
     */
    @JsonProperty(required = true)
    private DateTime startTime;

    /**
     * The end time of the aggregation interval.
     */
    @JsonProperty(required = true)
    private DateTime endTime;

    /**
     * The size of virtual machines in the pool. All VMs in a pool are the
     * same size.
     */
    @JsonProperty(required = true)
    private String vmSize;

    /**
     * The total core hours used in the pool during this aggregation interval.
     */
    @JsonProperty(required = true)
    private double totalCoreHours;

    /**
     * The cross data center network ingress in GiB to the pool during this
     * interval.
     */
    @JsonProperty(required = true)
    private double dataIngressGiB;

    /**
     * The cross data center network egress in GiB from the pool during this
     * interval.
     */
    @JsonProperty(required = true)
    private double dataEgressGiB;

    /**
     * Get the poolId value.
     *
     * @return the poolId value
     */
    public String poolId() {
        return this.poolId;
    }

    /**
     * Set the poolId value.
     *
     * @param poolId the poolId value to set
     * @return the PoolUsageMetrics object itself.
     */
    public PoolUsageMetrics withPoolId(String poolId) {
        this.poolId = poolId;
        return this;
    }

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
     * @return the PoolUsageMetrics object itself.
     */
    public PoolUsageMetrics withStartTime(DateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Get the endTime value.
     *
     * @return the endTime value
     */
    public DateTime endTime() {
        return this.endTime;
    }

    /**
     * Set the endTime value.
     *
     * @param endTime the endTime value to set
     * @return the PoolUsageMetrics object itself.
     */
    public PoolUsageMetrics withEndTime(DateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Get the vmSize value.
     *
     * @return the vmSize value
     */
    public String vmSize() {
        return this.vmSize;
    }

    /**
     * Set the vmSize value.
     *
     * @param vmSize the vmSize value to set
     * @return the PoolUsageMetrics object itself.
     */
    public PoolUsageMetrics withVmSize(String vmSize) {
        this.vmSize = vmSize;
        return this;
    }

    /**
     * Get the totalCoreHours value.
     *
     * @return the totalCoreHours value
     */
    public double totalCoreHours() {
        return this.totalCoreHours;
    }

    /**
     * Set the totalCoreHours value.
     *
     * @param totalCoreHours the totalCoreHours value to set
     * @return the PoolUsageMetrics object itself.
     */
    public PoolUsageMetrics withTotalCoreHours(double totalCoreHours) {
        this.totalCoreHours = totalCoreHours;
        return this;
    }

    /**
     * Get the dataIngressGiB value.
     *
     * @return the dataIngressGiB value
     */
    public double dataIngressGiB() {
        return this.dataIngressGiB;
    }

    /**
     * Set the dataIngressGiB value.
     *
     * @param dataIngressGiB the dataIngressGiB value to set
     * @return the PoolUsageMetrics object itself.
     */
    public PoolUsageMetrics withDataIngressGiB(double dataIngressGiB) {
        this.dataIngressGiB = dataIngressGiB;
        return this;
    }

    /**
     * Get the dataEgressGiB value.
     *
     * @return the dataEgressGiB value
     */
    public double dataEgressGiB() {
        return this.dataEgressGiB;
    }

    /**
     * Set the dataEgressGiB value.
     *
     * @param dataEgressGiB the dataEgressGiB value to set
     * @return the PoolUsageMetrics object itself.
     */
    public PoolUsageMetrics withDataEgressGiB(double dataEgressGiB) {
        this.dataEgressGiB = dataEgressGiB;
        return this;
    }

}
