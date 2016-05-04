/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import org.joda.time.DateTime;
import org.joda.time.Period;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Resource usage statistics for a job.
 */
public class JobStatisticsInner {
    /**
     * Gets or sets the URL for the statistics.
     */
    @JsonProperty(required = true)
    private String url;

    /**
     * Gets or sets the start time of the time range covered by the statistics.
     */
    @JsonProperty(required = true)
    private DateTime startTime;

    /**
     * Gets or sets the time at which the statistics were last updated. All
     * statistics are limited to the range between StartTime and
     * LastUpdateTime.
     */
    @JsonProperty(required = true)
    private DateTime lastUpdateTime;

    /**
     * Gets or sets the total user mode CPU time (summed across all cores and
     * all compute nodes) consumed by all the tasks in the job.
     */
    @JsonProperty(required = true)
    private Period userCPUTime;

    /**
     * Gets or sets the total kernel mode CPU time (summed across all cores
     * and all compute nodes) consumed by all the tasks in the job.
     */
    @JsonProperty(required = true)
    private Period kernelCPUTime;

    /**
     * Gets or sets the total wall clock time of all the tasks in the job.
     */
    @JsonProperty(required = true)
    private Period wallClockTime;

    /**
     * Gets or sets the total number of I/O read operations performed by all
     * the tasks in the job.
     */
    @JsonProperty(required = true)
    private long readIOps;

    /**
     * Gets or sets the total number of I/O write operations performed by all
     * the tasks in the job.
     */
    @JsonProperty(required = true)
    private long writeIOps;

    /**
     * Gets or sets the total amount of data in GiB of I/O read by all the
     * tasks in the job.
     */
    @JsonProperty(required = true)
    private double readIOGiB;

    /**
     * Gets or sets the total amount of data in GiB of I/O written by all the
     * tasks in the job.
     */
    @JsonProperty(required = true)
    private double writeIOGiB;

    /**
     * Gets or sets the total number of tasks successfully completed in the
     * job during the given time range.
     */
    @JsonProperty(required = true)
    private long numSucceededTasks;

    /**
     * Gets or sets the total number of tasks in the job that failed during
     * the given time range.
     */
    @JsonProperty(required = true)
    private long numFailedTasks;

    /**
     * Gets or sets the total number of retries on all the tasks in the job
     * during the given time range.
     */
    @JsonProperty(required = true)
    private long numTaskRetries;

    /**
     * Gets or sets the total wait time of all the tasks in the job.  The wait
     * time for a task is defined as the elapsed time between the creation of
     * the task creation and the start of task execution.  This value is
     * reported only in the account lifetime statistics; it is not included
     * in individual job statistics.
     */
    @JsonProperty(required = true)
    private Period waitTime;

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String url() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param url the url value to set
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setUrl(String url) {
        this.url = url;
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
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setStartTime(DateTime startTime) {
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
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setLastUpdateTime(DateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    /**
     * Get the userCPUTime value.
     *
     * @return the userCPUTime value
     */
    public Period userCPUTime() {
        return this.userCPUTime;
    }

    /**
     * Set the userCPUTime value.
     *
     * @param userCPUTime the userCPUTime value to set
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setUserCPUTime(Period userCPUTime) {
        this.userCPUTime = userCPUTime;
        return this;
    }

    /**
     * Get the kernelCPUTime value.
     *
     * @return the kernelCPUTime value
     */
    public Period kernelCPUTime() {
        return this.kernelCPUTime;
    }

    /**
     * Set the kernelCPUTime value.
     *
     * @param kernelCPUTime the kernelCPUTime value to set
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setKernelCPUTime(Period kernelCPUTime) {
        this.kernelCPUTime = kernelCPUTime;
        return this;
    }

    /**
     * Get the wallClockTime value.
     *
     * @return the wallClockTime value
     */
    public Period wallClockTime() {
        return this.wallClockTime;
    }

    /**
     * Set the wallClockTime value.
     *
     * @param wallClockTime the wallClockTime value to set
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setWallClockTime(Period wallClockTime) {
        this.wallClockTime = wallClockTime;
        return this;
    }

    /**
     * Get the readIOps value.
     *
     * @return the readIOps value
     */
    public long readIOps() {
        return this.readIOps;
    }

    /**
     * Set the readIOps value.
     *
     * @param readIOps the readIOps value to set
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setReadIOps(long readIOps) {
        this.readIOps = readIOps;
        return this;
    }

    /**
     * Get the writeIOps value.
     *
     * @return the writeIOps value
     */
    public long writeIOps() {
        return this.writeIOps;
    }

    /**
     * Set the writeIOps value.
     *
     * @param writeIOps the writeIOps value to set
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setWriteIOps(long writeIOps) {
        this.writeIOps = writeIOps;
        return this;
    }

    /**
     * Get the readIOGiB value.
     *
     * @return the readIOGiB value
     */
    public double readIOGiB() {
        return this.readIOGiB;
    }

    /**
     * Set the readIOGiB value.
     *
     * @param readIOGiB the readIOGiB value to set
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setReadIOGiB(double readIOGiB) {
        this.readIOGiB = readIOGiB;
        return this;
    }

    /**
     * Get the writeIOGiB value.
     *
     * @return the writeIOGiB value
     */
    public double writeIOGiB() {
        return this.writeIOGiB;
    }

    /**
     * Set the writeIOGiB value.
     *
     * @param writeIOGiB the writeIOGiB value to set
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setWriteIOGiB(double writeIOGiB) {
        this.writeIOGiB = writeIOGiB;
        return this;
    }

    /**
     * Get the numSucceededTasks value.
     *
     * @return the numSucceededTasks value
     */
    public long numSucceededTasks() {
        return this.numSucceededTasks;
    }

    /**
     * Set the numSucceededTasks value.
     *
     * @param numSucceededTasks the numSucceededTasks value to set
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setNumSucceededTasks(long numSucceededTasks) {
        this.numSucceededTasks = numSucceededTasks;
        return this;
    }

    /**
     * Get the numFailedTasks value.
     *
     * @return the numFailedTasks value
     */
    public long numFailedTasks() {
        return this.numFailedTasks;
    }

    /**
     * Set the numFailedTasks value.
     *
     * @param numFailedTasks the numFailedTasks value to set
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setNumFailedTasks(long numFailedTasks) {
        this.numFailedTasks = numFailedTasks;
        return this;
    }

    /**
     * Get the numTaskRetries value.
     *
     * @return the numTaskRetries value
     */
    public long numTaskRetries() {
        return this.numTaskRetries;
    }

    /**
     * Set the numTaskRetries value.
     *
     * @param numTaskRetries the numTaskRetries value to set
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setNumTaskRetries(long numTaskRetries) {
        this.numTaskRetries = numTaskRetries;
        return this;
    }

    /**
     * Get the waitTime value.
     *
     * @return the waitTime value
     */
    public Period waitTime() {
        return this.waitTime;
    }

    /**
     * Set the waitTime value.
     *
     * @param waitTime the waitTime value to set
     * @return the JobStatisticsInner object itself.
     */
    public JobStatisticsInner setWaitTime(Period waitTime) {
        this.waitTime = waitTime;
        return this;
    }

}
