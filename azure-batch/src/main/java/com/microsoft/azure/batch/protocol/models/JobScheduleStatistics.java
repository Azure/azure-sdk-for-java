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
 * Resource usage statistics for a job schedule.
 */
public class JobScheduleStatistics {
    /**
     * The URL of the statistics.
     */
    @JsonProperty(required = true)
    private String url;

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
     * The total user mode CPU time (summed across all cores and all compute
     * nodes) consumed by all tasks in all jobs created under the schedule.
     */
    @JsonProperty(required = true)
    private Period userCPUTime;

    /**
     * The total kernel mode CPU time (summed across all cores and all compute
     * nodes) consumed by all tasks in all jobs created under the schedule.
     */
    @JsonProperty(required = true)
    private Period kernelCPUTime;

    /**
     * The total wall clock time of all the tasks in all the jobs created
     * under the schedule.
     */
    @JsonProperty(required = true)
    private Period wallClockTime;

    /**
     * The total number of disk read operations made by all tasks in all jobs
     * created under the schedule.
     */
    @JsonProperty(required = true)
    private long readIOps;

    /**
     * The total number of disk write operations made by all tasks in all jobs
     * created under the schedule.
     */
    @JsonProperty(required = true)
    private long writeIOps;

    /**
     * The total gibibytes read from disk by all tasks in all jobs created
     * under the schedule.
     */
    @JsonProperty(required = true)
    private double readIOGiB;

    /**
     * The total gibibytes written to disk by all tasks in all jobs created
     * under the schedule.
     */
    @JsonProperty(required = true)
    private double writeIOGiB;

    /**
     * The total number of tasks successfully completed during the given time
     * range in jobs created under the schedule. A task completes
     * successfully if it returns exit code 0.
     */
    @JsonProperty(required = true)
    private long numSucceededTasks;

    /**
     * The total number of tasks that failed during the given time range in
     * jobs created under the schedule. A task fails if it exhausts its
     * maximum retry count without returning exit code 0.
     */
    @JsonProperty(required = true)
    private long numFailedTasks;

    /**
     * The total number of retries during the given time range on all tasks in
     * all jobs created under the schedule.
     */
    @JsonProperty(required = true)
    private long numTaskRetries;

    /**
     * The total wait time of all tasks in all jobs created under the
     * schedule. The wait time for a task is defined as the elapsed time
     * between the creation of the task and the start of task execution. (If
     * the task is retried due to failures, the wait time is the time to the
     * most recent task execution.).
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withUrl(String url) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withStartTime(DateTime startTime) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withLastUpdateTime(DateTime lastUpdateTime) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withUserCPUTime(Period userCPUTime) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withKernelCPUTime(Period kernelCPUTime) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withWallClockTime(Period wallClockTime) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withReadIOps(long readIOps) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withWriteIOps(long writeIOps) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withReadIOGiB(double readIOGiB) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withWriteIOGiB(double writeIOGiB) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withNumSucceededTasks(long numSucceededTasks) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withNumFailedTasks(long numFailedTasks) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withNumTaskRetries(long numTaskRetries) {
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
     * @return the JobScheduleStatistics object itself.
     */
    public JobScheduleStatistics withWaitTime(Period waitTime) {
        this.waitTime = waitTime;
        return this;
    }

}
